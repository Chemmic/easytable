package org.vandeseer.easytable.structure.cell;

import org.vandeseer.easytable.drawing.Drawer;
import org.vandeseer.easytable.drawing.cell.RotateTextCellDrawer;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(toBuilder = true)
public class RotateTextCell extends AbstractTextCell {

    @NonNull
    private String text;
    
    //Prefered over degree
    @Builder.Default
    private Angle angle = null;

    // 0 = normal text
    @Builder.Default
    private double degree = Angle.LEFTTORIGHT.getDegree();
    
    protected Drawer createDefaultDrawer() {
        return new RotateTextCellDrawer(this);
    }

    public static enum Angle  {
    	LEFTTORIGHT(0), BOTTOMTOTOP(90), TOPTOBOTTOM(270), RIGHTTOLEFT(180);
    	
    	private double degree;
    	private Angle(double degree) {
    		this.degree = degree;
    	}
    	
    	public double getDegree() {
    		return this.degree;
    	}
    }
}