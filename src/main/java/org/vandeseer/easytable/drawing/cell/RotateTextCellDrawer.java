package org.vandeseer.easytable.drawing.cell;

import static org.vandeseer.easytable.settings.HorizontalAlignment.CENTER;
import static org.vandeseer.easytable.settings.HorizontalAlignment.RIGHT;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.util.Matrix;
import org.vandeseer.easytable.drawing.DrawingContext;
import org.vandeseer.easytable.settings.VerticalAlignment;
import org.vandeseer.easytable.structure.cell.RotateTextCell;
import org.vandeseer.easytable.util.PdfUtil;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor
public class RotateTextCellDrawer extends AbstractCellDrawer<RotateTextCell> {


    public RotateTextCellDrawer(RotateTextCell cell) {
        this.cell = cell;
    }
    
    @Override
    @SneakyThrows
    public void drawContent(DrawingContext drawingContext) {
    	final double angle = cell.getAngle() == null ? cell.getDegree() : cell.getAngle().getDegree();
        final float startX = drawingContext.getStartingPoint().x;
        final float startY = drawingContext.getStartingPoint().y;
        drawingContext.getContentStream().setNonStrokingColor(Color.green);
        drawingContext.getContentStream().setStrokingColor(Color.green);
        drawingContext.getContentStream().lineTo(startX +20, startY + 0);

        final PDFont currentFont = cell.getFont();
        final int currentFontSize = cell.getFontSize();
        final Color currentTextColor = cell.getTextColor();

        float yOffset = startY + cell.getPaddingBottom();

        float height = cell.getRow().getHeight();

        if (cell.getRowSpan() > 1) {
            float rowSpanAdaption = cell.calculateHeightForRowSpan() - cell.getRow().getHeight();
            yOffset -= rowSpanAdaption;
            height = cell.calculateHeightForRowSpan();
        }

        final List<String> lines = cell.isWordBreak()
                ? PdfUtil.getOptimalTextBreakLines(cell.getText(), currentFont, currentFontSize, (height - cell.getVerticalPadding()))
                : Collections.singletonList(cell.getText());

        float textHeight = 0;
        
        float textWidth = (PdfUtil.getFontHeight(currentFont, currentFontSize) // font height
                + PdfUtil.getFontHeight(currentFont, currentFontSize) * cell.getLineSpacing()) * lines.size(); // line spacing;

        
        for (String line : lines) {
            float currentHeight = PdfUtil.getStringWidth(line, currentFont, currentFontSize);
            textHeight = currentHeight > textHeight ? currentHeight : textHeight;
        }
        
        
        if (cell.isVerticallyAligned(VerticalAlignment.MIDDLE)) {
        	if(angle >= 180) {
        		yOffset += (height + textHeight - cell.getPaddingTop() - cell.getPaddingBottom()) /2;
        	} else {
            yOffset += (height - textHeight - cell.getPaddingTop() - cell.getPaddingBottom()) / 2;
        	}
        } else if (cell.isVerticallyAligned(VerticalAlignment.TOP)) {
            yOffset += (height - textHeight - cell.getPaddingTop() - cell.getPaddingBottom());
        }
        
        float xOffset = startX ; //- PdfUtil.getFontHeight(currentFont, currentFontSize);
        if(angle < 180) {
        	//Text seems to be some pixels off the real starting position, no problem on > 180 but looks bad on < 180, therefore add 2
        	xOffset -= PdfUtil.getFontHeight(currentFont, currentFontSize) -  cell.getPaddingLeft();
        } else {
        	xOffset += PdfUtil.getFontHeight(currentFont, currentFontSize) +  cell.getPaddingLeft(); ;
        }
        drawingContext.getContentStream().moveTo(xOffset, yOffset);
        drawingContext.getContentStream().lineTo(xOffset +20, yOffset + 0);
        if (cell.isHorizontallyAligned(CENTER)) {
            xOffset = xOffset + ((cell.getWidth() - cell.getPaddingRight() - cell.getPaddingLeft()) / 2 - textWidth / 2);
        } else if (cell.isHorizontallyAligned(RIGHT)) {
            xOffset = xOffset + cell.getWidth() - cell.getPaddingRight() - cell.getPaddingLeft() - textWidth;
        }

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if(i != 0) {
            xOffset += (
                    PdfUtil.getFontHeight(currentFont, currentFontSize) // font height
                            + (i > 0 ? PdfUtil.getFontHeight(currentFont, currentFontSize) * cell.getLineSpacing() : 0f) // line spacing
            );
            }
            //Use Angle if set, else use custom degree
            drawText(line, currentFont, currentFontSize, currentTextColor, xOffset, yOffset, drawingContext.getContentStream(), angle);
        }
    }

    // TODO this is currently not used!
    @Override
    protected float calculateInnerHeight() {
        return 0;
    }

    // Angle in degrees e.g. 90 etc.
    protected void drawText(String text, PDFont font, int fontSize, Color color, float x, float y, PDPageContentStream contentStream, double angle) throws IOException {
        final AffineTransform transform = AffineTransform.getTranslateInstance(x, y);
        transform.concatenate(AffineTransform.getRotateInstance(Math.toRadians(angle)));
        transform.concatenate(AffineTransform.getTranslateInstance(-x, -y - fontSize));

        contentStream.moveTo(x, y);
        contentStream.beginText();
        contentStream.setTextMatrix(new Matrix(transform));
        contentStream.setNonStrokingColor(color);
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
        contentStream.setCharacterSpacing(0);
    }
}
