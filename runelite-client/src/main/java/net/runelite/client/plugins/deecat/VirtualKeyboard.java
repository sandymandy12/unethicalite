package dev.hoot.fighter;

import net.runelite.api.Point;
import net.runelite.client.plugins.deecat.Utility;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.time.Instant;

public class VirtualKeyboard extends Robot
{
    public VirtualKeyboard() throws AWTException
    {
        super();
    }

    public static void click(int x, int y) {

        Robot bot = null;
        try {
            bot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
        bot.mouseMove(x + 4, y + 27);
        long now = Instant.now().toEpochMilli();
        Utility.sleep(300);
        bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }
    public static void click(Point point) {

        Robot bot = null;
        try {
            bot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        };
        bot.mouseMove(point.getX() + 4, point.getY() + 27);
        bot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        bot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    public static void sendKeys(int keyCombination)
    {
        VirtualKeyboard kb = null;
        try {
            kb = new VirtualKeyboard();
            kb.keyPress(keyCombination);
            kb.keyRelease(keyCombination);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

}
