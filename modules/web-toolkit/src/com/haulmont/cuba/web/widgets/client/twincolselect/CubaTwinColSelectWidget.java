/*
 * Copyright (c) 2008-2016 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.haulmont.cuba.web.widgets.client.twincolselect;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.vaadin.client.connectors.AbstractMultiSelectConnector.MultiSelectWidget;
import com.vaadin.client.ui.VButton;
import com.vaadin.client.ui.VTwinColSelect;
import elemental.json.JsonObject;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class CubaTwinColSelectWidget extends VTwinColSelect {

    protected boolean addAllBtnEnabled;

    protected VButton addAll;

    protected VButton removeAll;

    protected HandlerRegistration addAllHandlerRegistration;

    protected HandlerRegistration removeAllHandlerRegistration;

    public CubaTwinColSelectWidget() {
        addItemsLeftToRightButton.setText(">");
        addItemsLeftToRightButton.addStyleName("add");
        removeItemsRightToLeftButton.setText("<");
        removeItemsRightToLeftButton.addStyleName("remove");
    }

    @Override
    protected DoubleClickListBox createOptionsBox() {
        return new CubaDoubleClickListBox();
    }

    @Override
    protected DoubleClickListBox createSelectionsBox() {
        return new CubaDoubleClickListBox();
    }

    @Override
    public void setItems(List<JsonObject> items) {
        // filter selected items
        List<JsonObject> selection = items.stream()
                .filter(MultiSelectWidget::isSelected)
                .collect(Collectors.toList());
        items.removeAll(selection);

        updateListBox(items, optionsListBox, (listBox, _items) -> {
            updateListBox(listBox, _items);
            afterUpdatesOptionsBox(_items);
        });

        updateListBox(selection, selectionsListBox, (listBox, _items) -> {
            updateListBox(listBox, _items);
            afterUpdatesSelectionsBox(_items);
        });
    }

    protected void updateListBox(List<JsonObject> items, ListBox listBox, BiConsumer<ListBox, List<JsonObject>> updateTask) {
        List<String> selectedItems = null;

        int selectedIdx = listBox.getSelectedIndex();
        int itemsCount = listBox.getItemCount();

        if (selectedIdx >= 0) {
            selectedItems = new ArrayList<>();
            for (int i = selectedIdx; i < itemsCount; i++) {
                selectedItems.add(listBox.getItemText(i));
            }
        }

        updateTask.accept(listBox, items);

        if (selectedItems != null) {
            // re-set selection
            for (int i = 0; i < itemsCount; i++) {
                String item = listBox.getItemText(i);
                listBox.setItemSelected(i, selectedItems.contains(item));
            }
        }
    }

    @Override
    protected void moveSelectedItemsLeftToRight() {
        Set<String> movedItems = moveSelectedItems(optionsListBox, selectionsListBox);
        selectionChangeListeners.forEach(listener -> listener.accept(movedItems,
                Collections.emptySet()));
    }

    @Override
    protected void moveSelectedItemsRightToLeft() {
        Set<String> movedItems = moveSelectedItems(selectionsListBox, optionsListBox);
        selectionChangeListeners.forEach(listener -> listener
                .accept(Collections.emptySet(), movedItems));
    }

    // CAUTION: copied from superclass
    protected static Set<String> moveSelectedItems(ListBox source, ListBox target) {
        final boolean[] sel = getSelectionBitmap(source);
        final Set<String> movedItems = new HashSet<>();
        for (int i = 0; i < sel.length; i++) {
            if (sel[i]) {
                final int optionIndex = i
                        - (sel.length - source.getItemCount());
                movedItems.add(source.getValue(optionIndex));

                // Move selection to another column
                final String text = source.getItemText(optionIndex);
                final String value = source.getValue(optionIndex);
                target.addItem(text, value);
                target.setItemSelected(target.getItemCount() - 1, true);
                source.removeItem(optionIndex);
            }
        }

        target.setFocus(true);

        return movedItems;
    }

    @Override
    public void onClick(ClickEvent event) {
        super.onClick(event);
        if (addAllBtnEnabled) {
            if (event.getSource() == addAll) {
                addAll();
            } else if (event.getSource() == removeAll) {
                removeAll();
            }
        }
    }

    private Set<String> moveAllItems(ListBox source, ListBox target) {
        final Set<String> movedItems = new HashSet<String>();
        int size = source.getItemCount();
        for (int i = 0; i < size; i++) {
            movedItems.add(source.getValue(i));
            final String text = source.getItemText(i);
            final String value = source.getValue(i);
            target.addItem(text, value);
            target.setItemSelected(target.getItemCount() - 1, true);
        }
        target.setFocus(true);
        if (source.getItemCount() > 0) {
            target.setSelectedIndex(0);
        }
        source.clear();
        return movedItems;
    }

    protected void addAll() {
        Set<String> movedItems = moveAllItems(optionsListBox, selectionsListBox);

        selectionChangeListeners.forEach(listener ->
                listener.accept(movedItems, Collections.emptySet()));
    }

    protected void removeAll() {
        Set<String> movedItems = moveAllItems(selectionsListBox, optionsListBox);

        selectionChangeListeners.forEach(listener ->
                listener.accept(Collections.emptySet(), movedItems));
    }

    public boolean isAddAllBtnEnabled() {
        return addAllBtnEnabled;
    }

    public void setAddAllBtnEnabled(boolean addAllBtnEnabled) {
        if (addAllBtnEnabled != this.addAllBtnEnabled) {
            this.addAllBtnEnabled = addAllBtnEnabled;
            if (addAllBtnEnabled) {
                enableAddAllBtn();
            } else {
                disableAddAllBtn();
            }
        }
    }

    @Override
    protected void afterUpdatesOptionsBox(List<JsonObject> items) {
        int index = 0;
        for (JsonObject item : items) {
            CubaDoubleClickListBox cubaSelections = (CubaDoubleClickListBox) optionsListBox;
            if (item.hasKey("style")) {
                cubaSelections.setOptionClassName(index, item.getString("style"));
            } else {
                cubaSelections.removeClassName(index);
            }
            index++;
        }
    }

    @Override
    protected void afterUpdatesSelectionsBox(List<JsonObject> selection) {
        int index = 0;
        for (JsonObject item : selection) {
            CubaDoubleClickListBox cubaSelections = (CubaDoubleClickListBox) selectionsListBox;
            if (item.hasKey("style")) {
                cubaSelections.setOptionClassName(index, item.getString("style"));
            } else {
                cubaSelections.removeClassName(index);
            }
            index++;
        }
    }

    protected void enableAddAllBtn() {
        HTML br1 = new HTML("<span/>");
        br1.setStyleName(CLASSNAME + "-deco");
        buttons.add(br1);
        buttons.insert(br1, buttons.getWidgetIndex(addItemsLeftToRightButton) + 1);
        addAll = new VButton();
        addAll.setText(">>");
        addAll.addStyleName("addAll");
        addAllHandlerRegistration = addAll.addClickHandler(this);
        buttons.insert(addAll, buttons.getWidgetIndex(br1) + 1);

        HTML br2 = new HTML("<span/>");
        br2.setStyleName(CLASSNAME + "-deco");
        buttons.add(br2);

        removeAll = new VButton();
        removeAll.setText("<<");
        removeAll.addStyleName("removeAll");
        removeAllHandlerRegistration = removeAll.addClickHandler(this);
        buttons.add(removeAll);
    }

    protected void disableAddAllBtn() {
        addAll.removeFromParent();
        addAllHandlerRegistration.removeHandler();
        addAll = null;

        removeAll.removeFromParent();
        removeAllHandlerRegistration.removeHandler();
        removeAll = null;
    }

    public class CubaDoubleClickListBox extends DoubleClickListBox {
        public void setOptionClassName(int optionIndex, String className) {
            getOptionElement(optionIndex).setClassName(className);
        }

        public void removeClassName(int optionIndex) {
            Element option = getOptionElement(optionIndex);
            String className = option.getClassName();
            if (className != null && !className.isEmpty()) {
                option.removeClassName(className);
            }
        }

        protected Element getOptionElement(int optionIndex) {
            assert optionIndex >= 0 && getItemCount() > optionIndex;
            SelectElement select = getElement().cast();
            return select.getOptions().getItem(optionIndex);
        }
    }
}