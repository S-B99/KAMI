package me.zeroeightsix.kami.gui.kami.component;

import me.zeroeightsix.kami.gui.kami.Stretcherlayout;
import me.zeroeightsix.kami.gui.rgui.component.Component;
import me.zeroeightsix.kami.gui.rgui.component.container.OrganisedContainer;
import me.zeroeightsix.kami.gui.rgui.component.use.ColorInput;
import me.zeroeightsix.kami.gui.rgui.component.use.ColorSlider;
import me.zeroeightsix.kami.gui.rgui.component.use.ColorSquare;
import me.zeroeightsix.kami.gui.rgui.component.use.CheckButton;
import me.zeroeightsix.kami.gui.rgui.component.use.Slider;
import me.zeroeightsix.kami.gui.rgui.poof.PoofInfo;
import me.zeroeightsix.kami.gui.rgui.render.theme.Theme;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.impl.BooleanSetting;
import me.zeroeightsix.kami.setting.impl.EnumSetting;
import me.zeroeightsix.kami.setting.impl.ColorSetting;
import me.zeroeightsix.kami.setting.impl.numerical.DoubleSetting;
import me.zeroeightsix.kami.setting.impl.numerical.FloatSetting;
import me.zeroeightsix.kami.setting.impl.numerical.IntegerSetting;
import me.zeroeightsix.kami.setting.impl.numerical.NumberSetting;
import me.zeroeightsix.kami.util.Bind;
import me.zeroeightsix.kami.util.HSBColourHolder;

import java.util.Arrays;

/**
 * Created by 086 on 6/08/2017.
 */
public class SettingsPanel extends OrganisedContainer {

    Module module;

    public SettingsPanel(Theme theme, Module module) {
        super(theme, new Stretcherlayout(1));
        setAffectLayout(false);
        this.module = module;
        prepare();
    }

    @Override
    public void renderChildren() {
        super.renderChildren();
    }

    public Module getModule() {
        return module;
    }

    private void prepare() {
        getChildren().clear();
        if (module == null) {
            setVisible(false);
            return;
        }
        if (!module.settingList.isEmpty()) {
            for (Setting setting : module.settingList) {
                if (!setting.isVisible()) continue;
                String name = setting.getName();
                boolean isNumber = setting instanceof NumberSetting;
                boolean isBoolean = setting instanceof BooleanSetting;
                boolean isEnum = setting instanceof EnumSetting;
                boolean isColor = setting instanceof ColorSetting;

                if (setting.getValue() instanceof Bind) {
                    addChild(new BindButton("Bind", null, module));
                }

                if (isNumber) {
                    NumberSetting numberSetting = (NumberSetting) setting;
                    boolean isBound = numberSetting.isBound();

                    // Terrible terrible bug fix.
                    // I know, these parseDoubles look awful, but any conversions I tried here would end up with weird floating point conversion errors.
                    // This is really the easiest solution..
                    double value = Double.parseDouble(numberSetting.getValue().toString());
                    if (!isBound) {
                        UnboundSlider slider = new UnboundSlider(value, name, setting instanceof IntegerSetting);
                        slider.addPoof(new Slider.SliderPoof<UnboundSlider, Slider.SliderPoof.SliderPoofInfo>() {
                            @Override
                            public void execute(UnboundSlider component, SliderPoofInfo info) {
                                if (setting instanceof IntegerSetting)
                                    setting.setValue((int) info.getNewValue());
                                else if (setting instanceof FloatSetting)
                                    setting.setValue((float) info.getNewValue());
                                else if (setting instanceof DoubleSetting)
                                    setting.setValue(info.getNewValue());
                                setModule(module);
                            }
                        });
                        if (numberSetting.getMax() != null) slider.setMax(numberSetting.getMax().doubleValue());
                        if (numberSetting.getMin() != null) slider.setMin(numberSetting.getMin().doubleValue());
                        addChild(slider);
                    } else {
                        double min = Double.parseDouble(numberSetting.getMin().toString());
                        double max = Double.parseDouble(numberSetting.getMax().toString());
                        Slider slider = new Slider(
                                value, min, max,
                                Slider.getDefaultStep(min, max),
                                name,
                                setting instanceof IntegerSetting);
                        slider.addPoof(new Slider.SliderPoof<Slider, Slider.SliderPoof.SliderPoofInfo>() {
                            @Override
                            public void execute(Slider component, SliderPoofInfo info) {
                                if (setting instanceof IntegerSetting)
                                    setting.setValue((int) info.getNewValue());
                                else if (setting instanceof FloatSetting)
                                    setting.setValue((float) info.getNewValue());
                                else if (setting instanceof DoubleSetting)
                                    setting.setValue(info.getNewValue());
                            }
                        });
                        addChild(slider);
                    }
                } else if (isBoolean) {
                    CheckButton checkButton = new CheckButton(name, null);
                    checkButton.setToggled(((BooleanSetting) setting).getValue());
                    checkButton.addPoof(new CheckButton.CheckButtonPoof<CheckButton, CheckButton.CheckButtonPoof.CheckButtonPoofInfo>() {
                        @Override
                        public void execute(CheckButton checkButton1, CheckButtonPoofInfo info) {
                            if (info.getAction() == CheckButtonPoofInfo.CheckButtonPoofInfoAction.TOGGLE) {
                                setting.setValue(checkButton.isToggled());
                                setModule(module);
                            }
                        }
                    });
                    addChild(checkButton);
                } else if (isEnum) {
                    Class<? extends Enum> type = ((EnumSetting) setting).clazz;
                    Object[] con = type.getEnumConstants();
                    String[] modes = Arrays.stream(con).map(o -> o.toString().toUpperCase()).toArray(String[]::new);
                    EnumButton enumbutton = new EnumButton(name, null, modes);
                    enumbutton.addPoof(new EnumButton.EnumbuttonIndexPoof<EnumButton, EnumButton.EnumbuttonIndexPoof.EnumbuttonInfo>() {
                        @Override
                        public void execute(EnumButton component, EnumbuttonInfo info) {
                            setting.setValue(con[info.getNewIndex()]);
                            setModule(module);
                        }
                    });
                    enumbutton.setIndex(Arrays.asList(con).indexOf(setting.getValue()));
                    addChild(enumbutton);
                } else if (isColor) {
                    ColorSetting colorSetting = (ColorSetting) setting;
                    HSBColourHolder value = colorSetting.getValue();
                    //Color slider
                    ColorSlider colorSlider = new ColorSlider(value, name);
                    colorSlider.addPoof(new ColorSlider.ColorPoof<ColorSlider, ColorSlider.ColorPoof.ColorPoofInfo>() {
                        @Override
                        public void execute(ColorSlider component, ColorPoofInfo info) {
                            setting.setValue(info.getNewValue());
                        }
                    });
                    addChild(colorSlider);
                    value = colorSlider.getValue();
                    //Color saturation square
                    ColorSquare colorSquare = new ColorSquare(value, name);
                    colorSquare.addPoof(new ColorSquare.ColorPoof<ColorSquare, ColorSquare.ColorPoof.ColorPoofInfo>() {
                        @Override
                        public void execute(ColorSquare component, ColorPoofInfo info) {
                            setting.setValue(info.getNewValue());
                        }
                    });
                    colorSquare.setWidth(getWidth() - 30);
                    colorSquare.setHeight(getWidth() - 30);
                    addChild(colorSquare);
                    value = colorSquare.getValue();
                    //Color input
                    ColorInput colorInput = new ColorInput(value, name);
                    colorInput.addPoof(new ColorInput.ColorInputPoof<ColorInput, ColorInput.ColorInputPoof.ColorInputPoofInfo>() {
                        @Override
                        public void execute(ColorInput component, ColorInputPoofInfo info) {
                            setting.setValue(info.getNewValue());
                        }
                    });
                    addChild(colorInput);
                }
            }
        }
        if (children.isEmpty()) {
            setVisible(false);
            return;
        } else {
            setVisible(true);
            return;
        }
    }

    public void setModule(Module module) {
        this.module = module;
        setMinimumWidth((int) (getParent().getWidth() * .9f));
        prepare();
        //This probably isn't the best solution but it's the easiest
        int i = 0;
        int h = 0;
        setAffectLayout(false);
        for (Component component : children) {
            component.setWidth(getWidth() - 10);
            component.setX(5);
            if (i == 0) { component.setY(component.getY()); }
            else { component.setY(h); }
            h += component.getHeight() + 4;
            i++;
        }
    }
}
