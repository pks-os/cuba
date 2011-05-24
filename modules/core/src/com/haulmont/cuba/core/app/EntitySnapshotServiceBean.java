/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.cuba.core.app;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.Locator;
import com.haulmont.cuba.core.entity.BaseEntity;
import com.haulmont.cuba.core.entity.EntitySnapshot;
import com.haulmont.cuba.core.global.EntityDiff;
import com.haulmont.cuba.core.global.View;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * <p>$Id$</p>
 *
 * @author artamonov
 */
@Service(EntitySnapshotService.NAME)
public class EntitySnapshotServiceBean implements EntitySnapshotService {
    public List<EntitySnapshot> getSnapshots(MetaClass metaClass, UUID id) {
        EntitySnapshotAPI snapshotAPI = Locator.lookup(EntitySnapshotAPI.NAME);
        return snapshotAPI.getSnapshots(metaClass, id);
    }

    public EntitySnapshot createSnapshot(BaseEntity entity, View view) {
        EntitySnapshotAPI snapshotAPI = Locator.lookup(EntitySnapshotAPI.NAME);
        return snapshotAPI.createSnapshot(entity, view);
    }

    public BaseEntity extractEntity(EntitySnapshot snapshot) {
        EntitySnapshotAPI snapshotAPI = Locator.lookup(EntitySnapshotAPI.NAME);
        return snapshotAPI.extractEntity(snapshot);
    }

    public EntityDiff getDifference(EntitySnapshot first, EntitySnapshot second) {
        EntitySnapshotAPI snapshotAPI = Locator.lookup(EntitySnapshotAPI.NAME);
        return snapshotAPI.getDifference(first, second);
    }
}
