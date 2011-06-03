/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Dmitry Abramov
 * Created: 05.03.2009 14:10:16
 * $Id$
 */
package com.haulmont.cuba.gui.xml.layout.loaders;

import com.haulmont.cuba.gui.xml.layout.*;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.core.global.MetadataProvider;
import org.dom4j.Element;
import org.apache.commons.lang.StringUtils;

public class PickerFieldLoader extends AbstractFieldLoader{
    public PickerFieldLoader(Context context, LayoutLoaderConfig config, ComponentsFactory factory) {
        super(context, config, factory);
    }

    @Override
    public Component loadComponent(ComponentsFactory factory, Element element, Component parent) throws InstantiationException, IllegalAccessException {
        final PickerField component = (PickerField) super.loadComponent(factory, element, parent);

        assignFrame(component);

        String captionProperty = element.attributeValue("captionProperty");
        if (!StringUtils.isEmpty(captionProperty)) {
            component.setCaptionMode(CaptionMode.PROPERTY);
            component.setCaptionProperty(captionProperty);
        }

        final String metaClass = element.attributeValue("metaClass");
        if (!StringUtils.isEmpty(metaClass)) {
            component.setMetaClass(MetadataProvider.getSession().getClass(metaClass));
        }

        // The code below remains for backward compatibility only!

        final String lookupScreen = element.attributeValue("lookupScreen");
        if (!StringUtils.isEmpty(lookupScreen)) {
            PickerField.LookupAction action = (PickerField.LookupAction) component.getAction(PickerField.LookupAction.NAME);
            if (action != null)
                action.setLookupScreen(lookupScreen);
        }

        String caption = element.attributeValue("lookupCaption");
        if (caption != null) {
            PickerField.LookupAction action = (PickerField.LookupAction) component.getAction(PickerField.LookupAction.NAME);
            if (action != null)
                action.setCaption(loadResourceString(caption));
        }

        caption = element.attributeValue("clearCaption");
        if (caption != null) {
            PickerField.ClearAction action = (PickerField.ClearAction) component.getAction(PickerField.ClearAction.NAME);
            if (action != null)
                action.setCaption(loadResourceString(caption));
        }

        caption = element.attributeValue("lookupIcon");
        if (caption != null) {
            PickerField.LookupAction action = (PickerField.LookupAction) component.getAction(PickerField.LookupAction.NAME);
            if (action != null)
                action.setIcon(loadResourceString(caption));
        }

        caption = element.attributeValue("clearIcon");
        if (caption != null) {
            PickerField.ClearAction action = (PickerField.ClearAction) component.getAction(PickerField.ClearAction.NAME);
            if (action != null)
                action.setIcon(loadResourceString(caption));
        }

        return component;
    }
}
