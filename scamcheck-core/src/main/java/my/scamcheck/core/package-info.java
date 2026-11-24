/**
 * ScamCheck core: the domain model and the rules engine.
 *
 * <p>Plain Java 21 with no framework dependencies (ADR-004), so every rule can be
 * unit-tested in isolation and the engine can be reused from other adapters.
 * The enforcer plugin in this module's POM fails the build if Spring or Jakarta
 * classes are ever added.
 *
 * <p>Planned sub-packages (see docs/ARCHITECTURE.md §2): {@code model},
 * {@code extract}, {@code rule}, {@code rule.data}, {@code score}.
 */
package my.scamcheck.core;
