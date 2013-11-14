/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.cuba.desktop.gui.components;

import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.SplitPanel;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import javax.swing.*;
import java.util.*;

/**
 * @author krivopustov
 * @version $Id$
 */
public class DesktopSplitPanel
        extends DesktopAbstractComponent<JSplitPane>
        implements SplitPanel, Component.HasSettings {

    protected Map<String, Component> componentByIds = new HashMap<>();
    protected Collection<Component> ownComponents = new LinkedHashSet<>();

    public DesktopSplitPanel() {
        impl = new JSplitPane();
        impl.setResizeWeight(0.5);
    }

    @Override
    public int getOrientation() {
        return impl.getOrientation() == JSplitPane.HORIZONTAL_SPLIT ? ORIENTATION_HORIZONTAL : ORIENTATION_VERTICAL;
    }

    @Override
    public void setOrientation(int orientation) {
        impl.setOrientation(orientation == ORIENTATION_HORIZONTAL ? JSplitPane.HORIZONTAL_SPLIT : JSplitPane.VERTICAL_SPLIT);
    }

    @Override
    public void setSplitPosition(int pos) {
        if (pos < 0 || pos > 100)
            throw new IllegalArgumentException("Split position must be between 0 and 100");
        impl.setResizeWeight(pos / 100.0);
    }

    @Override
    public void setLocked(boolean locked) {
    }

    @Override
    public boolean isLocked() {
        return false;
    }

    @Override
    public void setPositionUpdateListener(PositionUpdateListener positionListener) {
        // not supported
    }

    @Override
    public PositionUpdateListener getPositionUpdateListener() {
        // not supported
        return null;
    }

    @Override
    public void add(Component component) {
        JComponent jComponent = DesktopComponentsHelper.getComposition(component);
        if (ownComponents.isEmpty())
            impl.setLeftComponent(jComponent);
        else
            impl.setRightComponent(jComponent);

        if (component.getId() != null) {
            componentByIds.put(component.getId(), component);
            if (frame != null) {
                frame.registerComponent(component);
            }
        }
        ownComponents.add(component);
    }

    @Override
    public void remove(Component component) {
        JComponent jComponent = DesktopComponentsHelper.getComposition(component);
        impl.remove(jComponent);

        if (component.getId() != null) {
            componentByIds.remove(component.getId());
        }
        ownComponents.remove(component);
    }

    @Override
    public <T extends Component> T getOwnComponent(String id) {
        return (T) componentByIds.get(id);
    }

    @Override
    public <T extends Component> T getComponent(String id) {
        return ComponentsHelper.getComponent(this, id);
    }

    @Override
    public Collection<Component> getOwnComponents() {
        return Collections.unmodifiableCollection(ownComponents);
    }

    @Override
    public Collection<Component> getComponents() {
        return ComponentsHelper.getComponents(this);
    }

    @Override
    public void applySettings(Element element) {
        Element e = element.element("position");
        if (e != null) {
            String value = e.attributeValue("value");
            if (!StringUtils.isBlank(value)) {
                impl.setDividerLocation(Integer.valueOf(value));
            }
        }
    }

    @Override
    public boolean saveSettings(Element element) {
        int location = impl.getLastDividerLocation();
        if (location < 0)
            return false; // most probably user didn't change the divider location

        Element e = element.element("position");
        if (e == null)
            e = element.addElement("position");
        e.addAttribute("value", String.valueOf(location));
        return true;
    }
}