package com.enjin.core;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.UUID;

public class EnjinAPI {
    private List<Predicate<UUID>> vanishRegistrations = Lists.newArrayList();

    @Deprecated
    public void registerVanishPredicate(Predicate<UUID> predicate) {
        if (predicate == null) {
            return;
        }

        vanishRegistrations.add(predicate);
    }

    @Deprecated
    public Boolean getVanishState(UUID uuid) {
        boolean state = false;

        for (Predicate<UUID> registration : vanishRegistrations) {
            if (registration.apply(uuid) == true) {
                state = true;
                break;
            }
        }

        return state;
    }
}
