package mdtnh;

import mdtnh.energy.EnergySpec;
import mindustry.type.ItemStack;
import mindustry.type.LiquidStack;

/**
 * 使用 RecipeCrafter 的物品/液体 IO 与 MDT EnergyState，但把 recipe.energyPerCraftJ 解释为发电量。
 * 这样蒸汽轮机、内燃机、燃气轮机、太阳能与避雷针都能直接作为 MdtEnergyNode 接入现有电网。
 */
public class GtGeneratorCrafter extends RecipeCrafter {
    public GtGeneratorCrafter(String name) {
        super(name);
        energySource = EnergySource.electricity;
        energySpec.role = EnergySpec.Role.generator;
        energySpec.maxInputA = 0;
        energySpec.maxOutputA = 1;
        buildType = GtGeneratorBuild::new;
    }

    public class GtGeneratorBuild extends MDTFactoryBuild {
        @Override
        public void created() {
            super.created();
            if (getEffectiveGroups().length > 0 && selectedGroup < 0) selectedGroup = 0;
        }

        @Override
        public void updateTile() {
            RecipeGroup[] allGroups = getEffectiveGroups();
            if (allGroups.length == 0) return;
            if (selectedGroup < 0 || selectedGroup >= allGroups.length) selectedGroup = 0;

            Recipe[] activeRecipes = allGroups[selectedGroup].recipes;
            if (activeRecipes == null || activeRecipes.length == 0) return;

            if (currentRecipe < 0 || currentRecipe >= activeRecipes.length || !hasGeneratorInputs(activeRecipes[currentRecipe])) {
                currentRecipe = -1;
                for (int i = 0; i < activeRecipes.length; i++) {
                    if (hasGeneratorInputs(activeRecipes[i])) {
                        currentRecipe = i;
                        progress = 0f;
                        break;
                    }
                }
            }

            if (currentRecipe < 0) {
                progress = 0f;
                return;
            }

            Recipe active = activeRecipes[currentRecipe];
            float produced = Math.max(0f, active.energyPerCraftJ);
            if (energyState.energyJ + produced > energySpec.capacityJ + 0.0001f) return;

            float time = Math.max(0.0001f, active.craftTime);
            progress += delta() / time;
            while (progress >= 0.999999f) {
                if (!hasGeneratorInputs(active)) {
                    currentRecipe = -1;
                    progress = 0f;
                    return;
                }
                if (energyState.energyJ + produced > energySpec.capacityJ + 0.0001f) return;

                consumeGeneratorInputs(active);
                energyState.add(produced, energySpec());
                progress -= 1f;
            }
        }

        private boolean hasGeneratorInputs(Recipe recipe) {
            if (recipe.inputItems != null) {
                for (ItemStack stack : recipe.inputItems) {
                    if (stack != null && items.get(stack.item) < stack.amount) return false;
                }
            }
            if (recipe.inputLiquids != null) {
                for (LiquidStack stack : recipe.inputLiquids) {
                    if (stack != null && liquids.get(stack.liquid) + 0.0001f < stack.amount) return false;
                }
            }
            return true;
        }

        private void consumeGeneratorInputs(Recipe recipe) {
            if (recipe.inputItems != null) {
                for (ItemStack stack : recipe.inputItems) {
                    if (stack != null) items.remove(stack.item, stack.amount);
                }
            }
            if (recipe.inputLiquids != null) {
                for (LiquidStack stack : recipe.inputLiquids) {
                    if (stack != null) liquids.remove(stack.liquid, stack.amount);
                }
            }
        }
    }
}
