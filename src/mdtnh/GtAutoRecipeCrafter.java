package mdtnh;

/** RecipeCrafter 的自动选择首分组版本，适合泵、锅炉等不需要编程电路选择的设备。 */
public class GtAutoRecipeCrafter extends RecipeCrafter {
    public GtAutoRecipeCrafter(String name) {
        super(name);
        buildType = GtAutoBuild::new;
    }

    public class GtAutoBuild extends MDTFactoryBuild {
        @Override
        public void created() {
            super.created();
            if (getEffectiveGroups().length > 0 && selectedGroup < 0) selectedGroup = 0;
        }
    }
}
