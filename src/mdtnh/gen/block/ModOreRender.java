package mdtnh.gen.block;

import arc.Core;
import arc.Events;
import arc.input.KeyCode;

import mindustry.Vars;
import mindustry.game.EventType.Trigger;

public final class ModOreRender{

    private ModOreRender(){
    }

    /**
     * F8 控制的纯客户端显示状态。
     */
    public static boolean renderOres = true;

    private static boolean installed = false;

    private static final String settingKey =
            "mdtnh-render-ores";


    public static void install(){

        if(
                installed ||
                        Vars.headless
        ){
            return;
        }

        installed = true;

        renderOres =
                Core.settings.getBool(
                        settingKey,
                        true
                );


        Events.run(
                Trigger.update,
                () -> {

                    if(
                            Vars.state == null ||
                                    Vars.state.isMenu()
                    ){
                        return;
                    }

                    if(
                            !Core.input.keyTap(
                                    KeyCode.f8
                            )
                    ){
                        return;
                    }


                    renderOres =
                            !renderOres;


                    Core.settings.put(
                            settingKey,
                            renderOres
                    );


                    /*
                     * floor/overlay 使用缓存渲染，
                     * 切换后必须重新 cache。
                     */
                    OreExplorationController
                            .recacheAllOres();


                    if(Vars.ui != null){

                        Vars.ui.showInfoToast(
                                renderOres
                                        ? "[accent]矿物显示：开启"
                                        : "[gray]矿物显示：关闭",
                                2f
                        );
                    }
                }
        );
    }
}
