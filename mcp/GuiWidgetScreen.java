package net.minecraft.src;

import net.minecraft.client.Minecraft;
import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.input.lwjgl.LWJGLInput;
import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
import de.matthiasmann.twl.theme.ThemeManager;

/**
 * TWL Widget that switches out child widgets. the Minecraft gui end and twl
 * rendering is managed from GuiModScreen.
 * 
 * @author lahwran
 */
public class GuiWidgetScreen extends Widget {
	/**
	 * The initialized instance of GuiWidgetScreen.
	 */
	public static GuiWidgetScreen instance;
	/**
	 * The height of the screen that the widget will render on.
	 */
	public static int screenheight;
	/**
	 * The width of the screen that the widget will render on.
	 */
	public static int screenwidth;

	/**
	 * get the instance of GuiWidget, creating it if needed
	 * 
	 * @return GuiWidgetScreen singleton
	 */
	public static GuiWidgetScreen getInstance() {
		if (GuiWidgetScreen.instance != null) {
			return GuiWidgetScreen.instance;
		}
		try {
			GuiWidgetScreen.instance = new GuiWidgetScreen();
			GuiWidgetScreen.instance.renderer = new LWJGLRenderer();
			String themename = "twlGuiTheme.xml";
			GuiWidgetScreen.instance.gui = new GUI(GuiWidgetScreen.instance,
					GuiWidgetScreen.instance.renderer, new LWJGLInput());
			ModSettings.dbgout(GuiWidgetScreen.class.getClassLoader()
					.getResource(themename).toString());
			GuiWidgetScreen.instance.theme = ThemeManager.createThemeManager(
					GuiWidgetScreen.class.getClassLoader().getResource(
							themename), GuiWidgetScreen.instance.renderer);
			if (GuiWidgetScreen.instance.theme == null) {
				throw new RuntimeException(
						"I don't think you installed the theme correctly ...");
			}
			GuiWidgetScreen.instance.setTheme("");
			GuiWidgetScreen.instance.gui
					.applyTheme(GuiWidgetScreen.instance.theme);
			GuiWidgetScreen.instance.minecraftInstance = ModSettings
					.getMcinst();
			GuiWidgetScreen.instance.screenSize = new ScaledResolution(
					GuiWidgetScreen.instance.minecraftInstance.gameSettings,
					GuiWidgetScreen.instance.minecraftInstance.displayWidth,
					GuiWidgetScreen.instance.minecraftInstance.displayHeight);
		} catch (Throwable e) {
			e.printStackTrace();
			RuntimeException e2 = new RuntimeException("error loading theme");
			e2.initCause(e);
			throw e2;
		}
		return GuiWidgetScreen.instance;
	}

	/**
	 * The widget that is currently displayed.
	 */
	public Widget currentWidget = null;
	/**
	 * This is a reference to a TWL class that is used to render the widgets.
	 */
	public GUI gui = null;
	/**
	 * This is a reference to Minecraft.
	 */
	public Minecraft minecraftInstance;
	/**
	 * This is the rendered used by TWL.
	 */
	public LWJGLRenderer renderer = null;
	/**
	 * This is the ScaledResolution class that is used to scale all of the
	 * widgets.
	 */
	public ScaledResolution screenSize = null;
	/**
	 * This the the ThemeManager for GuiAPI.
	 */
	public ThemeManager theme = null;

	/**
	 * This creates a new instance of GuiWidgetScreen. It should only be used
	 * internally. Please use the static method getInstance() instead.
	 */
	public GuiWidgetScreen() {
	}

	@Override
	public void layout() {
		screenSize = new ScaledResolution(minecraftInstance.gameSettings,
				minecraftInstance.displayWidth, minecraftInstance.displayHeight);
		if (currentWidget != null) {
			GuiWidgetScreen.screenwidth = screenSize.getScaledWidth();
			GuiWidgetScreen.screenheight = screenSize.getScaledHeight();
			currentWidget.setSize(GuiWidgetScreen.screenwidth,
					GuiWidgetScreen.screenheight);
			currentWidget.setPosition(0, 0);
		}
	}

	/**
	 * Removes all children and clears the current widget.
	 */
	public void resetScreen() {
		removeAllChildren();
		currentWidget = null;
	}

	/**
	 * to be called only from GuiModScreen, sets the widget to display.
	 * GuiModScreen manages this.
	 * 
	 * @param widget
	 *            widget to display
	 */
	public void setScreen(Widget widget) {
		gui.resyncTimerAfterPause();
		gui.clearKeyboardState();
		gui.clearMouseState();
		removeAllChildren();
		add(widget);
		currentWidget = widget;
	}
}
