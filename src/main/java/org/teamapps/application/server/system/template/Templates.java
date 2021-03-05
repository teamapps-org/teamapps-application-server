package org.teamapps.application.server.system.template;

import org.teamapps.common.format.Color;
import org.teamapps.ux.component.format.*;
import org.teamapps.ux.component.template.BaseTemplate;
import org.teamapps.ux.component.template.Template;
import org.teamapps.ux.component.template.gridtemplate.*;

public class Templates {

	public final static String PROPERTY_IMAGE = BaseTemplate.PROPERTY_IMAGE;
	public final static String PROPERTY_ICON = BaseTemplate.PROPERTY_ICON;
	public final static String PROPERTY_LINE1_LEFT_ICON = "lin1LeftIcon";
	public final static String PROPERTY_LINE1_RIGHT_ICON = "lin1RightIcon";
	public final static String PROPERTY_LINE1 = BaseTemplate.PROPERTY_CAPTION;
	public final static String PROPERTY_LINE1_BADGE = BaseTemplate.PROPERTY_BADGE;
	public final static String PROPERTY_LINE2 = "line2";
	public final static String PROPERTY_LINE2_RIGHT = "line2Right";
	public final static String PROPERTY_LINE3 = "line3";
	public final static String PROPERTY_LINE3_RIGHT = "line3Right";
	public final static String PROPERTY_LINE4 = "line4";




	public static Template create4LinesTemplate() {
		GridTemplate tpl = new GridTemplate()
				.setPadding(new Spacing(1))
				.setGridGap(0)
				.addColumn(SizingPolicy.AUTO)
				.addColumn(SizingPolicy.FRACTION)
				.addRow(SizeType.FIXED, 18, 18, 0, 0)
				.addRow(SizeType.FIXED, 18, 18, 0, 0)
				.addRow(SizeType.FIXED, 18, 18, 0, 0)
				.addRow(SizeType.FIXED, 18, 18, 0, 0)
				.addElement(new ImageElement(PROPERTY_IMAGE, 0, 0, 68, 68).setRowSpan(4)
						.setBorder(new Border(new Line(Color.GRAY, LineType.SOLID, 0.5f)).setBorderRadius(300))
						//.setShadow(Shadow.withSize(0.5f))
						.setVerticalAlignment(VerticalElementAlignment.CENTER)
						.setMargin(new Spacing(0, 8, 0, 4)))
				.addElement(new IconElement(PROPERTY_ICON, 0, 0, 64).setRowSpan(4)
						.setVerticalAlignment(VerticalElementAlignment.CENTER)
						.setMargin(new Spacing(0, 8, 0, 4)))
				.addElement(new FloatingElement(0, 1)
						.addElement(new IconElement(PROPERTY_LINE1_LEFT_ICON, 0, 0, 16)
								.setMargin(new Spacing(0, 4, 0, 0)))
						.addElement(new TextElement(PROPERTY_LINE1, 0, 0)
								.setWrapLines(false)
								.setFontStyle(new FontStyle(1f, Color.MATERIAL_GREY_900, null, true, false, false)))
						.setVerticalAlignment(VerticalElementAlignment.CENTER)
						.setHorizontalAlignment(HorizontalElementAlignment.LEFT))
				.addElement(new TextElement(PROPERTY_LINE2, 1, 1)
						.setWrapLines(false)
						.setFontStyle(new FontStyle(1f, Color.MATERIAL_GREY_700, null, false, false, false))
						.setVerticalAlignment(VerticalElementAlignment.CENTER)
						.setHorizontalAlignment(HorizontalElementAlignment.LEFT))
				.addElement(new TextElement(PROPERTY_LINE3, 2, 1)
						.setWrapLines(false)
						.setFontStyle(new FontStyle(1f, Color.MATERIAL_GREY_700, null, false, false, false))
						.setVerticalAlignment(VerticalElementAlignment.CENTER)
						.setHorizontalAlignment(HorizontalElementAlignment.LEFT))
				.addElement(new TextElement(PROPERTY_LINE4, 3, 1)
						.setWrapLines(false)
						.setFontStyle(new FontStyle(1f, Color.MATERIAL_BLUE_900, null, true, false, false))
						.setVerticalAlignment(VerticalElementAlignment.CENTER)
						.setHorizontalAlignment(HorizontalElementAlignment.LEFT));
		tpl.setBorder(new Border(new Line(Color.MATERIAL_GREY_300, LineType.SOLID, 0.5f), null, null, null));
		return tpl;
	}
}
