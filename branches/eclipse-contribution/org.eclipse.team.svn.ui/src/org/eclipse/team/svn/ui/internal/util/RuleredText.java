/*******************************************************************************
 * Copyright (c) 2005, 2006 Subclipse project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Subclipse project committers - initial API and implementation
 ******************************************************************************/
package org.eclipse.team.svn.ui.internal.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.svn.ui.internal.ISVNUIConstants;
import org.eclipse.ui.PlatformUI;

public class RuleredText extends StyledText {
    private int width = 80;

    public RuleredText(Composite parent, int style) {
        super(parent, style); 
        setFont();
    }

    public RuleredText(Composite parent, int style, int width) {
        this(parent, style); 
        this.width = width;
        addPaintListener(getPaintListener());
    }
    
    private void setFont() {
        Font commentFont = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getFontRegistry().get(ISVNUIConstants.SVN_COMMENT_FONT);
        if (commentFont != null) setFont(commentFont);
    }

    private PaintListener getPaintListener() {
        PaintListener listener = new PaintListener() {
            public void paintControl(PaintEvent e) {
                FontMetrics fm = e.gc.getFontMetrics (); 
                Rectangle rect = getClientArea();
                int x1 = rect.x + (fm.getAverageCharWidth() * width) - (fm.getAverageCharWidth() * getHorizontalIndex());
                int y1 = rect.y;
                int x2 = x1;
                int y2 = y1 + rect.height;
                Color saveColor = e.gc.getForeground();
                e.gc.setForeground(getShell().getDisplay().getSystemColor(SWT.COLOR_GRAY));
                e.gc.drawLine(x1, y1, x2, y2);
                e.gc.setForeground(saveColor);
            }           
        };
        return listener;
    }

}