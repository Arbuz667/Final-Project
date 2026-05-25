package com.softchaos.ai;

import com.softchaos.entities.Enemy;
import com.softchaos.entities.Player;

/** Strategy interface for enemy behaviour. Each implementation defines movement and attack logic. */
public interface EnemyAI {
    void update(Enemy self, Player player, float delta);
}
