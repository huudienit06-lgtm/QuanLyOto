package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import javax.swing.Icon;

final class MenuIcon implements Icon {
    enum Type { USER, USERS, CAR, PART, TOOL, TAG, CLOCK, SEARCH, DOCUMENT, MONEY, SECURITY, BACKUP, SETTINGS, CHART, KEY, EXIT }

    private static final Color BLUE = new Color(36, 99, 235);
    private final Type type;
    private final Color color;

    MenuIcon(Type type) {
        this(type, BLUE);
    }

    MenuIcon(Type type, Color color) {
        this.type = type;
        this.color = color;
    }

    @Override
    public int getIconWidth() { return 18; }

    @Override
    public int getIconHeight() { return 18; }

    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.translate(x, y);
        g.setColor(color);
        g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        switch (type) {
            case USER:
                g.drawOval(6, 1, 6, 6);
                g.drawRoundRect(3, 9, 12, 8, 6, 6);
                break;
            case USERS:
                g.drawOval(2, 2, 5, 5); g.drawOval(11, 2, 5, 5);
                g.drawArc(0, 8, 9, 9, 0, 180); g.drawArc(8, 8, 9, 9, 0, 180);
                break;
            case CAR:
                Path2D car = new Path2D.Float();
                car.moveTo(2, 11); car.lineTo(4, 6); car.lineTo(7, 4); car.lineTo(13, 4);
                car.lineTo(16, 9); car.lineTo(16, 13); car.lineTo(2, 13); car.closePath();
                g.draw(car); g.drawOval(4, 11, 3, 3); g.drawOval(12, 11, 3, 3);
                break;
            case PART:
                g.drawRect(2, 4, 14, 12); g.drawLine(2, 8, 9, 11);
                g.drawLine(16, 8, 9, 11); g.drawLine(9, 11, 9, 16);
                break;
            case TOOL:
                g.drawLine(4, 14, 13, 5); g.drawArc(1, 1, 8, 8, 35, 235); g.drawOval(11, 11, 4, 4);
                break;
            case TAG:
                Path2D tag = new Path2D.Float();
                tag.moveTo(2, 6); tag.lineTo(9, 2); tag.lineTo(16, 9); tag.lineTo(9, 16); tag.closePath();
                g.draw(tag); g.drawOval(6, 5, 2, 2);
                break;
            case CLOCK:
                g.drawOval(2, 2, 14, 14); g.drawLine(9, 4, 9, 9); g.drawLine(9, 9, 13, 11);
                break;
            case SEARCH:
                g.drawOval(2, 2, 10, 10); g.drawLine(10, 10, 16, 16);
                break;
            case DOCUMENT:
                g.drawRoundRect(4, 1, 11, 16, 2, 2);
                g.drawLine(6, 6, 13, 6); g.drawLine(6, 9, 13, 9); g.drawLine(6, 12, 11, 12);
                break;
            case MONEY:
                g.drawOval(3, 2, 11, 5); g.drawArc(3, 4, 11, 5, 0, -180);
                g.drawArc(3, 7, 11, 5, 0, -180); g.drawArc(3, 10, 11, 5, 0, -180);
                break;
            case SECURITY:
                Path2D shield = new Path2D.Float();
                shield.moveTo(9, 1); shield.lineTo(16, 4); shield.lineTo(15, 11);
                shield.lineTo(9, 17); shield.lineTo(3, 11); shield.lineTo(2, 4); shield.closePath();
                g.draw(shield); g.drawLine(6, 9, 8, 11); g.drawLine(8, 11, 12, 6);
                break;
            case BACKUP:
                g.drawRoundRect(2, 2, 14, 14, 2, 2); g.drawRect(5, 2, 8, 5); g.drawRect(5, 10, 8, 6);
                break;
            case CHART:
                g.drawLine(2, 16, 2, 2); g.drawLine(2, 16, 17, 16);
                g.fillRect(5, 10, 3, 6); g.fillRect(10, 6, 3, 10); g.fillRect(15, 3, 2, 13);
                break;
            case KEY:
                g.drawOval(1, 4, 8, 8); g.drawLine(9, 8, 17, 8);
                g.drawLine(14, 8, 14, 11); g.drawLine(16, 8, 16, 10);
                break;
            case EXIT:
                g.drawRoundRect(1, 2, 9, 14, 2, 2); g.drawLine(7, 9, 17, 9);
                g.drawLine(13, 5, 17, 9); g.drawLine(13, 13, 17, 9);
                break;
            case SETTINGS:
            default:
                g.drawOval(5, 5, 8, 8); g.drawOval(7, 7, 4, 4);
                for (int angle = 0; angle < 360; angle += 45) {
                    double radians = Math.toRadians(angle);
                    int x1 = (int) (9 + Math.cos(radians) * 6);
                    int y1 = (int) (9 + Math.sin(radians) * 6);
                    int x2 = (int) (9 + Math.cos(radians) * 8);
                    int y2 = (int) (9 + Math.sin(radians) * 8);
                    g.drawLine(x1, y1, x2, y2);
                }
                break;
        }
        g.dispose();
    }
}
