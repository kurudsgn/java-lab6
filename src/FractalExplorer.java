import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class FractalExplorer {

    public static void main(String[] args)
    {
        FractalExplorer displayExplorer = new FractalExplorer(600);
        displayExplorer.createAndShowGUI();
        displayExplorer.drawFractal();
    }
    private int size;
    private JImageDisplay image;
    private FractalGenerator gen;
    private Rectangle2D.Double range;
    private int rowsRemain = 0;
    private JComboBox<FractalGenerator> fractalSelector;

    JButton reset = new JButton();
    JButton save = new JButton();
    public FractalExplorer(int s) {
        size = s;
        gen = new Mandelbrot();
        range = new Rectangle2D.Double();
        gen.getInitialRange(range);
        image = new JImageDisplay(size, size);
    }

    public void createAndShowGUI() {
        image.setLayout(new BorderLayout());
        JFrame frame = new JFrame("Fractal Explorer");
        frame.add(image, BorderLayout.CENTER);
        reset = new JButton("Reset");
        save = new JButton("Save");
        JLabel label = new JLabel("Fractal:");
        fractalSelector = new JComboBox<>();
        fractalSelector.addItem(new Mandelbrot());
        fractalSelector.addItem(new Tricorn());
        fractalSelector.addItem(new BurningShip());

        ButtonClick resetHandler = new ButtonClick();
        ButtonClick saveHandler = new ButtonClick();
        ComboBoxClick comboHandler = new ComboBoxClick();
        reset.addActionListener(resetHandler);
        save.addActionListener(saveHandler);
        fractalSelector.addActionListener(comboHandler);




        MouseHandler click = new MouseHandler();
        image.addMouseListener(click);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel myPanel = new JPanel();
        myPanel.add(label, BorderLayout.CENTER);
        myPanel.add(fractalSelector, BorderLayout.CENTER);
        frame.add(myPanel, BorderLayout.NORTH);

        JPanel myBottomPanel = new JPanel();
        myBottomPanel.add(reset);
        myBottomPanel.add(save);
        frame.add(myBottomPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);
        frame.setResizable(false);
    }

    private void drawFractal() {
        enableUI(false);
        rowsRemain = size;
        for (int y = 0; y < size; y++) {
            FractalWorker drawRow = new FractalWorker(y);
            drawRow.execute();
        }
    }

    private class FractalWorker extends SwingWorker<Object, Object>{
        private final int currentY;

        private ArrayList<Integer> pixelColors;

        public FractalWorker(int currentY) {
            this.currentY = currentY;
        }

        @Override
        public Object doInBackground() {
            pixelColors = new ArrayList<>(size);
            for (int x = 0; x < size; x++) {
                int count = gen.numIterations(
                        FractalGenerator.getCoord(range.x, range.x + range.width, size, x),
                        FractalGenerator.getCoord(range.y, range.y + range.width, size, currentY)
                );
                int rgbColor;
                if (count == -1) {
                    rgbColor = 0;
                } else {
                    float hue = 0.7f + (float) count / 200f;
                    rgbColor = Color.HSBtoRGB(hue, 1f, 1f);
                }
                pixelColors.add(rgbColor);
            }
            return null;
        }

        @Override
        public void done() {
            for (int x = 0; x < size; x++) {
                image.drawPixel(x, currentY, pixelColors.get(x));
            }
            image.repaint(0, 0, currentY, size, 1);
            rowsRemain--;
            if (rowsRemain == 0)
                enableUI(true);
        }
    }
    private class ButtonClick implements ActionListener {
        public void actionPerformed(ActionEvent e) {

            String command = e.getActionCommand();


            if (command.equals("Reset")) {
                gen.getInitialRange(range);
                drawFractal();
            }
            if (command.equals("Save")){
                JFileChooser fileChooser = new JFileChooser();
                FileNameExtensionFilter fileFilter = new FileNameExtensionFilter("PNG Images", "png");
                fileChooser.setFileFilter(fileFilter);
                fileChooser.setAcceptAllFileFilterUsed(false);
                int t = fileChooser.showSaveDialog(image);
                if (t == JFileChooser.APPROVE_OPTION) {
                    try {
                        ImageIO.write(image.img, "png", fileChooser.getSelectedFile());
                    } catch (Exception ee) {
                        JOptionPane.showMessageDialog(image, ee.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }
    private class ComboBoxClick implements ActionListener
    {
        public void actionPerformed(ActionEvent a){
            gen = (FractalGenerator) fractalSelector.getSelectedItem();
            gen.getInitialRange(range);
            drawFractal();
        }
    }
    private void enableUI(boolean val){
        reset.setEnabled(val);
        save.setEnabled(val);
        fractalSelector.setEnabled(val);
    }
    private class MouseHandler extends MouseAdapter
    {
        @Override
        public void mouseClicked(MouseEvent e)
        {
            if (rowsRemain > 0) {
                return;
            }
            int x = e.getX();
            double xCoord = gen.getCoord(range.x,
                    range.x + range.width, size, x);

            int y = e.getY();
            double yCoord = gen.getCoord(range.y, range.y + range.height, size, y);

            gen.recenterAndZoomRange(range, xCoord, yCoord, 0.5);

            drawFractal();
        }
    }
}
