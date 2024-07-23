package Processamento;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import javax.imageio.ImageIO;

public class ProcessamentoImagens {

    private LinkedList<BufferedImage> imagens = new LinkedList<>();
    private BufferedImage imagemCombinadaNormal;
    private BufferedImage imagemCombinadaDesfocada;
    private JFrame janela;
    private JPanel painelControle;
    private JPanel painelImagens;
    private JScrollPane scrollPanePessoa;
    private JScrollPane scrollPanePaisagem;
    private JScrollPane scrollPaneCombinadaNormal;
    private JScrollPane scrollPaneCombinadaDesfocada;
    private PainelImagem painelImagemPessoa;
    private PainelImagem painelImagemPaisagem;
    private PainelImagem painelImagemCombinadaNormal;
    private PainelImagem painelImagemCombinadaDesfocada;

    private JSlider controleTolerancia;
    private JLabel rotuloTolerancia;
    private JSlider controleBlur;
    private JLabel rotuloBlur;
    private JButton botaoZoomIn;
    private JButton botaoZoomOut;
    private JButton botaoToleranciaMais;
    private JButton botaoToleranciaMenos;
    private JButton botaoCarregarPessoa;
    private JButton botaoCarregarPaisagem;
    private JButton botaoCombinar;
    private PainelImagem JpanelAtual;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ProcessamentoImagens().criarEExibirGUI());
    }

    private void criarEExibirGUI() {
        janela = new JFrame("Combinador de Imagens");
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.setSize(1600, 1200); // Aumentar o tamanho da janela
        janela.setLayout(new BorderLayout());

        painelControle = new JPanel();
        painelControle.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        botaoCarregarPessoa = criarBotaoEstilizado("Carregar Imagem da Pessoa");
        botaoCarregarPaisagem = criarBotaoEstilizado("Carregar Imagem da Paisagem");
        botaoCombinar = criarBotaoEstilizado("Combinar Imagens");

        botaoCarregarPessoa.addActionListener(e -> carregarImagem("pessoa"));
        botaoCarregarPaisagem.addActionListener(e -> carregarImagem("paisagem"));

        botaoCombinar.addActionListener(e -> {
            if (imagens.size() >= 2) {
                BufferedImage imagemPessoa = redimensionarImagem(imagens.get(0), 800, 600);
                BufferedImage imagemPaisagem = redimensionarImagem(imagens.get(1), 800, 600);

                try {
                    int tolerancia = controleTolerancia.getValue();
                    int blur = controleBlur.getValue();
                    BufferedImage imagemPessoaComMascara = aplicarMascara(imagemPessoa, criarMascara(imagemPessoa, tolerancia));
                    imagemCombinadaNormal = combinarImagens(imagemPessoaComMascara, imagemPaisagem);
                    imagemCombinadaDesfocada = combinarImagens(imagemPessoaComMascara, aplicarDesfoque(imagemPaisagem, blur));

                    exibirImagens();

                    ImageIO.write(imagemCombinadaNormal, "png", new File("imagem_combinada_normal.png"));
                    ImageIO.write(imagemCombinadaDesfocada, "png", new File("imagem_combinada_desfocada.png"));

                    JOptionPane.showMessageDialog(janela, "Imagens combinadas e salvas com sucesso!");
                } catch (IOException excecaoIO) {
                    excecaoIO.printStackTrace();
                    JOptionPane.showMessageDialog(janela, "Erro ao salvar imagens.");
                }
            } else {
                JOptionPane.showMessageDialog(janela, "Por favor, carregue ambas as imagens primeiro.");
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        painelControle.add(botaoCarregarPessoa, gbc);

        gbc.gridy = 1;
        painelControle.add(botaoCarregarPaisagem, gbc);

        gbc.gridy = 2;
        painelControle.add(botaoCombinar, gbc);

        // Adicionar controles de tolerância
        rotuloTolerancia = new JLabel("Tolerância: 50");
        controleTolerancia = new JSlider(0, 255, 50);
        controleTolerancia.setMajorTickSpacing(50);
        controleTolerancia.setMinorTickSpacing(10);
        controleTolerancia.setPaintTicks(true);
        controleTolerancia.setPaintLabels(true);
        controleTolerancia.addChangeListener(e -> rotuloTolerancia.setText("Tolerância: " + controleTolerancia.getValue()));

        gbc.gridy = 3;
        gbc.gridwidth = 1;
        painelControle.add(rotuloTolerancia, gbc);

        gbc.gridx = 1;
        painelControle.add(controleTolerancia, gbc);

        // Botões para ajustar a tolerância
        botaoToleranciaMais = criarBotaoEstilizado("+ Tolerância");
        botaoToleranciaMenos = criarBotaoEstilizado("- Tolerância");

        botaoToleranciaMais.addActionListener(e -> controleTolerancia.setValue(Math.min(controleTolerancia.getValue() + 10, controleTolerancia.getMaximum())));
        botaoToleranciaMenos.addActionListener(e -> controleTolerancia.setValue(Math.max(controleTolerancia.getValue() - 10, controleTolerancia.getMinimum())));

        gbc.gridy = 4;
        gbc.gridx = 0;
        painelControle.add(botaoToleranciaMais, gbc);

        gbc.gridx = 1;
        painelControle.add(botaoToleranciaMenos, gbc);

        // Adicionar controles de desfoque
        rotuloBlur = new JLabel("Desfoque: 3");
        controleBlur = new JSlider(1, 10, 3);
        controleBlur.setMajorTickSpacing(2);
        controleBlur.setMinorTickSpacing(1);
        controleBlur.setPaintTicks(true);
        controleBlur.setPaintLabels(true);
        controleBlur.addChangeListener(e -> rotuloBlur.setText("Desfoque: " + controleBlur.getValue()));

        gbc.gridy = 5;
        gbc.gridx = 0;
        painelControle.add(rotuloBlur, gbc);

        gbc.gridx = 1;
        painelControle.add(controleBlur, gbc);

        // Botões para ajustar o zoom
        botaoZoomIn = criarBotaoEstilizado("Zoom +");
        botaoZoomOut = criarBotaoEstilizado("Zoom -");

        botaoZoomIn.addActionListener(e -> aplicarZoom(1.2)); // Aumenta o zoom em 20%
        botaoZoomOut.addActionListener(e -> aplicarZoom(0.8)); // Diminui o zoom em 20%

        gbc.gridy = 6;
        gbc.gridx = 0;
        painelControle.add(botaoZoomIn, gbc);

        gbc.gridx = 1;
        painelControle.add(botaoZoomOut, gbc);

        painelImagemPessoa = new PainelImagem("Imagem da Pessoa");
        painelImagemPaisagem = new PainelImagem("Imagem da Paisagem");
        painelImagemCombinadaNormal = new PainelImagem("Imagem Combinada Normal");
        painelImagemCombinadaDesfocada = new PainelImagem("Imagem Combinada Desfocada");

        scrollPanePessoa = new JScrollPane(painelImagemPessoa);
        scrollPanePaisagem = new JScrollPane(painelImagemPaisagem);
        scrollPaneCombinadaNormal = new JScrollPane(painelImagemCombinadaNormal);
        scrollPaneCombinadaDesfocada = new JScrollPane(painelImagemCombinadaDesfocada);

        scrollPanePessoa.setPreferredSize(new Dimension(600, 400));
        scrollPanePaisagem.setPreferredSize(new Dimension(600, 400));
        scrollPaneCombinadaNormal.setPreferredSize(new Dimension(600, 400));
        scrollPaneCombinadaDesfocada.setPreferredSize(new Dimension(600, 400));

        painelImagens = new JPanel();
        painelImagens.setLayout(new GridLayout(2, 2, 10, 10));
        painelImagens.add(scrollPanePessoa);
        painelImagens.add(scrollPanePaisagem);
        painelImagens.add(scrollPaneCombinadaNormal);
        painelImagens.add(scrollPaneCombinadaDesfocada);

        janela.add(painelControle, BorderLayout.WEST);
        janela.add(painelImagens, BorderLayout.CENTER);
        janela.pack();
        janela.setVisible(true);
    }

    private JButton criarBotaoEstilizado(String texto) {
        JButton botao = new JButton(texto);
        botao.setPreferredSize(new Dimension(200, 40));
        botao.setFont(new Font("Arial", Font.BOLD, 14));
        return botao;
    }

    private void carregarImagem(String tipo) {
        JFileChooser seletorDeArquivos = new JFileChooser();
        int resultado = seletorDeArquivos.showOpenDialog(janela);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File arquivo = seletorDeArquivos.getSelectedFile();
            try {
                BufferedImage imagem = ImageIO.read(arquivo);
                imagens.add(imagem);
                JOptionPane.showMessageDialog(janela, tipo + " imagem carregada com sucesso!");
                exibirImagens();
            } catch (IOException excecaoIO) {
                excecaoIO.printStackTrace();
                JOptionPane.showMessageDialog(janela, "Erro ao carregar imagem.");
            }
        }
    }

    private void exibirImagens() {
        if (imagens.size() > 0) {
            painelImagemPessoa.setImagem(imagens.get(0));
        }
        if (imagens.size() > 1) {
            painelImagemPaisagem.setImagem(imagens.get(1));
        }
        if (imagemCombinadaNormal != null) {
            painelImagemCombinadaNormal.setImagem(imagemCombinadaNormal);
        }
        if (imagemCombinadaDesfocada != null) {
            painelImagemCombinadaDesfocada.setImagem(imagemCombinadaDesfocada);
        }
    }

    private BufferedImage criarMascara(BufferedImage imagem, int tolerancia) {
        int largura = imagem.getWidth();
        int altura = imagem.getHeight();

        BufferedImage mascara = new BufferedImage(largura, altura, BufferedImage.TYPE_BYTE_BINARY);
        //Remoção de bits menos significativos
        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                int corPixel = imagem.getRGB(x, y);
                int r = (corPixel >> 16) & 0xFF;
                int g = (corPixel >> 8) & 0xFF;
                int b = corPixel & 0xFF;

                if (Math.abs(r - 255) <= tolerancia && Math.abs(g - 255) <= tolerancia && Math.abs(b - 255) <= tolerancia) {
                    mascara.setRGB(x, y, Color.BLACK.getRGB());
                } else {
                    mascara.setRGB(x, y, Color.WHITE.getRGB());
                }
            }
        }

        return mascara;
    }

    private BufferedImage aplicarMascara(BufferedImage imagem, BufferedImage mascara) {
        int largura = imagem.getWidth();
        int altura = imagem.getHeight();

        BufferedImage imagemComMascara = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                int corMascara = mascara.getRGB(x, y);
                if (corMascara == Color.BLACK.getRGB()) {
                    imagemComMascara.setRGB(x, y, 0x00FFFFFF & imagem.getRGB(x, y));
                } else {
                    imagemComMascara.setRGB(x, y, imagem.getRGB(x, y));
                }
            }
        }

        return imagemComMascara;
    }

    private BufferedImage combinarImagens(BufferedImage imagem1, BufferedImage imagem2) {
        int largura = Math.min(imagem1.getWidth(), imagem2.getWidth());
        int altura = Math.min(imagem1.getHeight(), imagem2.getHeight());

        BufferedImage imagemCombinada = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                int corImagem1 = imagem1.getRGB(x, y);
                int corImagem2 = imagem2.getRGB(x, y);
                   //checar somente primeiro Byte de transparência
                if ((corImagem1 & 0xFF000000) == 0) {
                    imagemCombinada.setRGB(x, y, corImagem2);
                } else {
                    imagemCombinada.setRGB(x, y, corImagem1);
                }
            }
        }

        return imagemCombinada;
    }

    private BufferedImage aplicarDesfoque(BufferedImage imagem, int tamanho) {
        float[] matrizDesfoque = new float[tamanho * tamanho];
        for (int i = 0; i < matrizDesfoque.length; i++) {
            matrizDesfoque[i] = 1.0f / (tamanho * tamanho);
        }
        Kernel kernel = new Kernel(tamanho, tamanho, matrizDesfoque);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        return op.filter(imagem, null);
    }

    private BufferedImage redimensionarImagem(BufferedImage imagem, int novaLargura, int novaAltura) {
        Image imagemRedimensionada = imagem.getScaledInstance(novaLargura, novaAltura, Image.SCALE_SMOOTH);
        BufferedImage imagemRedimensionadaBuffered = new BufferedImage(novaLargura, novaAltura, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = imagemRedimensionadaBuffered.createGraphics();
        g2d.drawImage(imagemRedimensionada, 0, 0, null);
        g2d.dispose();

        return imagemRedimensionadaBuffered;
    }

    private void aplicarZoom(double fator) {
        JpanelAtual.aplicarZoom(fator);
    }

    class PainelImagem extends JPanel {
        private BufferedImage imagem;
        private double zoom = 1.0; // Fator de zoom

        public PainelImagem(String titulo) {
            setPreferredSize(new Dimension(600, 400)); // Tamanho inicial do painel
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
            PainelImagem esteJPanel = this;
            addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    JpanelAtual=esteJPanel;
                }
                
                @Override
                public void mousePressed(MouseEvent e) {}

                @Override
                public void mouseReleased(MouseEvent e) {}

                @Override
                public void mouseEntered(MouseEvent e) {}

                @Override
                public void mouseExited(MouseEvent e) {}
            });
        }
        
         

        public void setImagem(BufferedImage imagem) {
            this.imagem = imagem;
            revalidate(); // Necessário para que o painel se ajuste
            repaint();
        }

        public void aplicarZoom(double fator) {
            zoom *= fator;
            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (imagem != null) {
                int largura = (int) (imagem.getWidth() * zoom);
                int altura = (int) (imagem.getHeight() * zoom);
                g.drawImage(imagem, 0, 0, largura, altura, null);
            }
        }
    }
}
