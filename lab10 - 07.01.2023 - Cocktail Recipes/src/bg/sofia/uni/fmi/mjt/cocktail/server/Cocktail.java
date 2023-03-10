package bg.sofia.uni.fmi.mjt.cocktail.server;

import java.util.Objects;
import java.util.Set;

public record Cocktail(String name, Set<Ingredient> ingredients) {
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Cocktail cocktail = (Cocktail) o;
        return Objects.equals(name, cocktail.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();

        string.append("# ").append(name()).append(System.lineSeparator());

        for (Ingredient ingredient : ingredients()) {
            string.append("   - ")
                    .append(ingredient.name())
                    .append(" -> ")
                    .append(ingredient.amount())
                    .append(System.lineSeparator());
        }

        return string.toString();
    }
}
