package vanguard.content;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import vanguard.sim.EchoSpec;

/**
 * M11 part G, step G2 (design/enemies/bosses, Boss rush; Level 49; m11g-gaps.md §1 #2–#7): a rush's echoes for the
 * simulation. An echo is its original's echo stat block ({@link EnemyData.BossData#echo()}, read from the overlay
 * {@code boss.echo}: {@link EchoOverlay}), built by {@link BossRules#boss} as any boss under the original's slug (its
 * sprites, sounds and looks are the original's), with its {@link EchoSpec} (the forming, the dissolve).
 */
final class EchoRules {
    private EchoRules() {}

    /** {@code slug}'s echo stat block, its attacks taken {@code from} ground units resolved (RingRules). */
    static EnemyData echo(Content content, String slug) {
        EnemyData echo = content.enemy(slug)
                .boss()
                .flatMap(EnemyData.BossData::echo)
                .orElseThrow(() -> new IllegalArgumentException(slug + ": no echo (boss.echo)"));
        List<String> problems = new ArrayList<>();
        EnemyData resolved = RingRules.resolve(slug + " (echo)", echo, content.enemies(), problems);
        if (!problems.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", problems));
        }
        return resolved;
    }

    /**
     * The echo's forming and dissolve for the simulation (step G5: an anchored echo's fire while it forms, {@code
     * form_fire}); empty for any other boss.
     */
    static Optional<EchoSpec> spec(EnemyData enemy, EnemyData.BossData script) {
        return script.dissolve().map(dissolve -> {
            EchoSpec built = new EchoSpec(script.form().orElse(0.0), dissolve);
            return script.formFire()
                    .map(fire -> built.firing(
                            attack(enemy, fire.attack()),
                            fire.every(),
                            fire.clear().orElse(0.0)))
                    .orElse(built);
        });
    }

    /** The index of {@code enemy}'s attack named {@code name}. */
    private static int attack(EnemyData enemy, String name) {
        for (int a = 0; a < enemy.attacks().size(); a++) {
            if (enemy.attacks().get(a).name().filter(name::equals).isPresent()) {
                return a;
            }
        }
        throw new IllegalArgumentException(enemy.name() + ": no attack '" + name + "' (boss.form_fire)");
    }
}
