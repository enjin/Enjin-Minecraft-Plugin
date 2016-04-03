package com.enjin.core;

import com.google.common.base.Predicate;

import java.util.List;
import java.util.UUID;

public class EnjinAPI {
	private List<Predicate<UUID>> vanishRegistrations;

	public void registerVanishPredicate(Predicate<UUID> predicate) {
		if (predicate == null) {
			return;
		}

		vanishRegistrations.add(predicate);
	}

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
