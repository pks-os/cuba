/*
 * Copyright (c) 2008-2017 Haulmont.
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
 */

package com.haulmont.cuba.gui.model.impl;

import com.haulmont.bali.events.Subscription;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.gui.model.CollectionContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 *
 */
public class CollectionContainerImpl<T extends Entity> extends InstanceContainerImpl<T> implements CollectionContainer<T> {

    private static final Logger log = LoggerFactory.getLogger(CollectionContainerImpl.class);

    protected List<T> collection = new ArrayList<>();

    public CollectionContainerImpl(MetaClass metaClass) {
        super(metaClass);
    }

    @Override
    public List<T> getItems() {
        return Collections.unmodifiableList(collection);
    }

    @Override
    public List<T> getMutableItems() {
        return new ObservableList<>(collection, () -> {
            clearItemIfNotExists();
            fireCollectionChanged();
        });
    }

    @Override
    public void setItems(@Nullable Collection<T> entities) {
        detachListener(collection);
        collection.clear();
        if (entities != null) {
            collection.addAll(entities);
            attachListener(collection);
        }
        clearItemIfNotExists();
        fireCollectionChanged();
    }

    @Nullable
    @Override
    public T getItem(Object entityId) {
        return collection.stream()
                .filter(entity -> entity.getId().equals(entityId))
                .findAny()
                .orElse(null);
    }

    @Override
    public T getItemNN(Object entityId) {
        T item = getItem(entityId);
        if (item == null)
            throw new IllegalArgumentException("Item with id='" + entityId + "' not found");
        return item;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subscription addCollectionChangeListener(Consumer<CollectionChangeEvent<T>> listener) {
        return events.subscribe(CollectionChangeEvent.class, (Consumer) listener);
    }

    protected void fireCollectionChanged() {
        CollectionChangeEvent<T> collectionChangeEvent = new CollectionChangeEvent<>(this);
        log.trace("collectionChanged: {}", collectionChangeEvent);
        events.publish(CollectionChangeEvent.class, collectionChangeEvent);
    }

    protected void attachListener(Collection<T> entities) {
        for (T entity : entities) {
            attachListener(entity);
        }
    }

    protected void detachListener(Collection<T> entities) {
        for (T entity : entities) {
            detachListener(entity);
        }
    }

    protected void clearItemIfNotExists() {
        if (item != null && !collection.contains(item)) {
            setItem(null);
        }
    }
}