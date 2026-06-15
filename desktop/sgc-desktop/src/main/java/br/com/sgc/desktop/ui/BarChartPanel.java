package br.com.sgc.desktop.ui;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Painel customizado que desenha um gráfico de barras simples,
 * sem depender de bibliotecas externas (apenas Graphics2D).
 *
 * Usado na tela de Relatórios para representar
 * graficamente as vendas por mês/ano.
 */
public class BarChartPanel extends JPanel {

    private Map<String, BigDecimal> dados = new LinkedHashMap<>();
    private String titulo = "";

    public BarChartPanel() {
        setPreferredSize(new Dimension(600, 300));
        setBackground(Color.WHITE);
    }

    public void setDados(Map<String, BigDecimal> dados, String titulo) {
        this.dados = dados != null ? dados : new LinkedHashMap<>();
        this.titulo = titulo != null ? titulo : "";
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        int marginLeft = 70;
        int marginRight = 20;
        int marginTop = 40;
        int marginBottom = 60;

        int chartWidth = width - marginLeft - marginRight;
        int chartHeight = height - marginTop - marginBottom;

        // Título
        g2.setColor(Color.DARK_GRAY);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.drawString(titulo, marginLeft, 22);

        if (dados.isEmpty()) {
            g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
            g2.drawString("Sem dados para exibir.", marginLeft, marginTop + chartHeight / 2);
            return;
        }

        // Valor máximo (para escala)
        BigDecimal max = BigDecimal.ZERO;
        for (BigDecimal v : dados.values()) {
            if (v.compareTo(max) > 0) {
                max = v;
            }
        }
        if (max.compareTo(BigDecimal.ZERO) == 0) {
            max = BigDecimal.ONE;
        }

        // Eixos
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawLine(marginLeft, marginTop, marginLeft, marginTop + chartHeight);
        g2.drawLine(marginLeft, marginTop + chartHeight, marginLeft + chartWidth, marginTop + chartHeight);

        // Linhas de referência horizontais (25%, 50%, 75%, 100%)
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        for (int i = 1; i <= 4; i++) {
            int y = marginTop + chartHeight - (chartHeight * i / 4);
            g2.setColor(new Color(230, 230, 230));
            g2.drawLine(marginLeft, y, marginLeft + chartWidth, y);

            BigDecimal valorRef = max.multiply(BigDecimal.valueOf(i)).divide(BigDecimal.valueOf(4));
            g2.setColor(Color.GRAY);
            g2.drawString(formatMoney(valorRef), 5, y + 4);
        }

        // Barras
        int n = dados.size();
        int gap = 10;
        int barWidth = Math.max(10, (chartWidth - gap * (n + 1)) / n);

        int x = marginLeft + gap;
        Color barColor = new Color(33, 110, 204);

        for (Map.Entry<String, BigDecimal> entry : dados.entrySet()) {
            BigDecimal valor = entry.getValue() != null ? entry.getValue() : BigDecimal.ZERO;

            double proporcao = valor.doubleValue() / max.doubleValue();
            int barHeight = (int) (chartHeight * proporcao);
            int y = marginTop + chartHeight - barHeight;

            g2.setColor(barColor);
            g2.fillRect(x, y, barWidth, barHeight);

            // Valor acima da barra
            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
            String valorStr = formatMoney(valor);
            int strWidth = g2.getFontMetrics().stringWidth(valorStr);
            g2.drawString(valorStr, x + (barWidth - strWidth) / 2, Math.max(y - 4, 12));

            // Rótulo abaixo do eixo
            String label = entry.getKey();
            g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
            int labelWidth = g2.getFontMetrics().stringWidth(label);
            g2.drawString(label, x + (barWidth - labelWidth) / 2, marginTop + chartHeight + 16);

            x += barWidth + gap;
        }
    }

    private String formatMoney(BigDecimal valor) {
        return "R$ " + valor.setScale(0, java.math.RoundingMode.HALF_UP);
    }
}
