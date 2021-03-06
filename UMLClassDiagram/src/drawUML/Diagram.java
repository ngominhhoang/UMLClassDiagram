package drawUML;

import analyzingProject.ClassObj;
import analyzingProject.ClassObjManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class Diagram extends JPanel {
    ClassObjManager manager ;
    ArrayList<Layer> listOfLayer = new ArrayList<Layer>();
    public Layer pickedLayer = null;
    int panelWidth = 1280;
    int panelHeight = 960;
    int lastMouseX = 0, lastMouseY = 0;
    int imageX = 30, imageY = 30;
    double scaleFactor = 1;
    boolean init = false;

    JMenuItem parameterItem = new JMenuItem("Show Parameter");
    JMenuItem visibleItem = new JMenuItem("Remove");
    JPopupMenu popupMenu = new JPopupMenu();

    public Diagram(ClassObjManager manager) {
        this.manager = manager;
        ArrayList<ClassObj> listOfObj = manager.getClassObjList();

        int index = 0, base = (int)(Math.sqrt(listOfObj.size()))+1;
        for (ClassObj obj : listOfObj) {

            Layer layer = new Layer(obj);
            //System.out.println(1);
            layer.setXaxis((index/base)*500+100);
            layer.setYaxis((index%base)*500+100);
            ++index;
            listOfLayer.add(layer);
        }
        //System.out.println(panelHeight);
        parameterItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pickedLayer != null) {
                    if (!pickedLayer.isShowingParameter()) {
                        pickedLayer.setShowParameter(true);
                    }
                    else {
                        pickedLayer.setShowParameter(false);
                    }
                    repaint();
                }
            }
        });
        visibleItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pickedLayer != null) {
                    pickedLayer.setVisible(false);
                    repaint();
                }
            }
        });

        popupMenu.add(parameterItem);
        popupMenu.add(visibleItem);

        MouseMotionHandler mouseHandler = new MouseMotionHandler();
        addMouseMotionListener(mouseHandler);
        addMouseListener(mouseHandler);
        addMouseWheelListener(mouseHandler);
    }

    public ArrayList<Layer> getListOfLayer() {
        return listOfLayer;
    }

    private void createLayer(Graphics2D g2D) {
        for (Layer layer : listOfLayer) {
            layer.setG2D(g2D);
            layer.optimizeSize();
        }
        LayerPosInitialiser posInit = new LayerPosInitialiser(listOfLayer);
        posInit.initialisePos();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension((int) (panelWidth*scaleFactor), (int) (panelHeight*scaleFactor));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;

        g2D.scale(scaleFactor,scaleFactor);

        if (!init) {
            createLayer(g2D);
            init = true;
        }

        panelHeight = 0;panelWidth = 0;
        for (Layer layer : listOfLayer) {
            if (!layer.isVisible()) continue;
            layer.setG2D(g2D);
            //layer.optimizeSize();
            panelWidth = Math.max(panelWidth,layer.getXaxis()+layer.getWidth()+20);
            panelHeight = Math.max(panelHeight,layer.getYaxis()+layer.getHeight()+20);
            layer.fixScaleHeight();
            //System.out.println(layer.getHeight());
            layer.renderBlock();
        }

        for (Layer layer : listOfLayer) {
            layer.renderRelations();
        }
    }

    class MouseMotionHandler extends MouseMotionAdapter implements
            MouseListener, MouseWheelListener {

        public void mousePressed(MouseEvent e) {
            lastMouseX = e.getX();
            lastMouseY = e.getY();

            for (Layer layer : listOfLayer) {
                layer.setLine_stroke(1);
            }
            repaint();

            if (pickedLayer == null) {
                return;
            }
            listOfLayer.remove(pickedLayer);
            listOfLayer.add(listOfLayer.size(),pickedLayer);
            pickedLayer.setLine_stroke(3);
            repaint();
        }

        public void mouseDragged(MouseEvent e) {
            //System.out.println("drag");
            /*for (int i = listOfLayer.size()-1; i>=0; --i) {
                Layer layer = listOfLayer.get(i);
                //Rectangle rec = new Rectangle(layer.getXaxis(),layer.getYaxis(),layer.getWidth(),layer.getHeight());
                if (layer.getLine_stroke() == 4) {

                    if (layer.getLine_stroke() == 1) break;
                    int xDiff = e.getX() - lastMouseX;
                    int yDiff = e.getY() - lastMouseY;
                    layer.setXaxis(layer.getXaxis() + (int)(xDiff/scaleFactor));
                    layer.setYaxis(layer.getYaxis() + (int)(yDiff/scaleFactor));
                    lastMouseX = e.getX();
                    lastMouseY = e.getY();
                    repaint();
                    break;
                    //layer.setLine_stroke(1);
                }
            }*/

            if (pickedLayer == null) return;
            int type = getCursor().getType();
            int xDiff = e.getX() - lastMouseX;
            int yDiff = e.getY() - lastMouseY;
            switch (type) {

                case Cursor.MOVE_CURSOR:
                    pickedLayer.setXaxis(pickedLayer.getXaxis() + (int)(xDiff/scaleFactor));
                    pickedLayer.setYaxis(pickedLayer.getYaxis() + (int)(yDiff/scaleFactor));
                    break;

                case Cursor.NW_RESIZE_CURSOR:
                    pickedLayer.setXaxis(pickedLayer.getXaxis() + (int)(xDiff/scaleFactor));
                    pickedLayer.setYaxis(pickedLayer.getYaxis() + (int)(yDiff/scaleFactor));
                    pickedLayer.setWidth(pickedLayer.getWidth() - (int)(xDiff/scaleFactor));
                    pickedLayer.setHeight(pickedLayer.getHeight() - (int)(yDiff/scaleFactor));
                    break;

                case Cursor.SE_RESIZE_CURSOR:
                    pickedLayer.setWidth(pickedLayer.getWidth() + (int)(xDiff/scaleFactor));
                    pickedLayer.setHeight(pickedLayer.getHeight() + (int)(yDiff/scaleFactor));
                    break;

                case Cursor.SW_RESIZE_CURSOR:
                    pickedLayer.setXaxis(pickedLayer.getXaxis() + (int)(xDiff/scaleFactor));
                    pickedLayer.setWidth(pickedLayer.getWidth() - (int)(xDiff/scaleFactor));
                    pickedLayer.setHeight(pickedLayer.getHeight() + (int)(yDiff/scaleFactor));
                    break;

                case Cursor.NE_RESIZE_CURSOR:
                    pickedLayer.setYaxis(pickedLayer.getYaxis() + (int)(yDiff/scaleFactor));
                    pickedLayer.setWidth(pickedLayer.getWidth() + (int)(xDiff/scaleFactor));
                    pickedLayer.setHeight(pickedLayer.getHeight() - (int)(yDiff/scaleFactor));
                    break;

                case Cursor.N_RESIZE_CURSOR:
                    pickedLayer.setYaxis(pickedLayer.getYaxis() + (int)(yDiff/scaleFactor));
                    pickedLayer.setHeight(pickedLayer.getHeight() - (int)(yDiff/scaleFactor));
                    break;

                case Cursor.S_RESIZE_CURSOR:
                    pickedLayer.setHeight(pickedLayer.getHeight() + (int)(yDiff/scaleFactor));
                    break;

                case Cursor.W_RESIZE_CURSOR:
                    pickedLayer.setXaxis(pickedLayer.getXaxis() + (int)(xDiff/scaleFactor));
                    pickedLayer.setWidth(pickedLayer.getWidth() - (int)(xDiff/scaleFactor));
                    break;

                case Cursor.E_RESIZE_CURSOR:
                    pickedLayer.setWidth(pickedLayer.getWidth() + (int)(xDiff/scaleFactor));
                    break;
            }

            lastMouseX = e.getX();
            lastMouseY = e.getY();
            repaint();
        }

        public void mouseMoved(MouseEvent e) {

            Point P = new Point((int) (e.getX()/scaleFactor),(int) (e.getY()/scaleFactor));
            boolean insideRec = false;
            for (int i = listOfLayer.size()-1;i>=0;--i) {
                Layer layer = listOfLayer.get(i);
                if (!layer.isVisible()) continue;
                int Xaxis = layer.getXaxis();
                int Yaxis = layer.getYaxis();
                int Width = (int) (layer.getWidth());
                int Height = (int) (layer.getHeight());

                Rectangle rec = new Rectangle(Xaxis,Yaxis,Width,Height);

                Rectangle recPointNW = new Rectangle(Xaxis,Yaxis,5,5);
                Rectangle recPointSW = new Rectangle(Xaxis,Yaxis+Height,5,5);
                Rectangle recPointNE = new Rectangle(Xaxis+Width,Yaxis,5,5);
                Rectangle recPointSE = new Rectangle(Xaxis+Width,Yaxis+Height,5,5);

                Rectangle recLineN = new Rectangle(Xaxis,Yaxis,Width,5);
                Rectangle recLineW = new Rectangle(Xaxis,Yaxis,5,Height);
                Rectangle recLineE = new Rectangle(Xaxis+Width,Yaxis,5,Height);
                Rectangle recLineS = new Rectangle(Xaxis,Yaxis+Height,Width,5);

                insideRec = true;
                pickedLayer = layer;
                if (recPointNW.contains(P)) {
                    setCursor(new Cursor(Cursor.NW_RESIZE_CURSOR));
                    break;
                }

                if (recPointSE.contains(P)) {
                    setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR));
                    break;
                }

                if (recPointSW.contains(P)) {
                    setCursor(new Cursor(Cursor.SW_RESIZE_CURSOR));
                    break;
                }

                if (recPointNE.contains(P)) {
                    setCursor(new Cursor(Cursor.NE_RESIZE_CURSOR));
                    break;
                }

                if (recLineE.contains(P)) {
                    setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
                    break;
                }

                if (recLineW.contains(P)) {
                    setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
                    break;
                }

                if (recLineN.contains(P)) {
                    setCursor(new Cursor(Cursor.N_RESIZE_CURSOR));
                    break;
                }

                if (recLineS.contains(P)) {
                    setCursor(new Cursor(Cursor.S_RESIZE_CURSOR));
                    break;
                }

                if (rec.contains(P)) {
                    setCursor(new Cursor(Cursor.MOVE_CURSOR));
                    break;
                }
                insideRec = false;
                pickedLayer = null;
            }

            if (!insideRec) setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }

        public void mouseWheelMoved(MouseWheelEvent e) {
            int notches = e.getWheelRotation();
            scaleFactor = scaleFactor + notches / 10.0;
            if (scaleFactor < 0.3) {
                scaleFactor = 0.3;
            } else if (scaleFactor > 3.0) {
                scaleFactor = 3.0;
            }
            repaint();
        }

        public void mouseReleased(MouseEvent e) {
            //System.out.println("release");
            if (pickedLayer == null) return;
            if (e.isPopupTrigger()) {
                if (!pickedLayer.isShowingParameter()) {
                    parameterItem.setText("Show Parameter");
                }
                    else {
                    parameterItem.setText("Hide Parameter");
                }
                popupMenu.show(e.getComponent(),e.getX(),e.getY());
            }
            repaint();
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mouseClicked(MouseEvent e) {
            //System.out.println("click");
        }
    }

}