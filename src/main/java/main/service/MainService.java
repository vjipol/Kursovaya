package main.service;

import main.entities.Recipe;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import main.repository.RecipeRepository;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MainService {

    @Autowired
    private RecipeRepository recipeRepository;

    // 1. Импорт из CSV
    public void importFromCsv(InputStream inputStream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            boolean skipHeader = true;
            while ((line = reader.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                String[] parts = line.split(",", -1);
                if (parts.length >= 4) {
                    Recipe recipe = new Recipe();
                    recipe.setTitle(parts[1]);
                    recipe.setIngredients(parts[2]);
                    recipe.setDescription(parts[3]);
                    if (parts.length >= 5) recipe.setLink(parts[4]);
                    recipeRepository.save(recipe);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading CSV", e);
        }
    }

    // 2. Экспорт в CSV
    public void exportToCsv(OutputStream outputStream) {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            writer.write("id,title,ingredients,description,link\n");
            for (Recipe recipe : recipeRepository.findAll()) {
                writer.write(String.format("%d,%s,%s,%s,%s\n",
                        recipe.getId(),
                        escapeCsv(recipe.getTitle()),
                        escapeCsv(recipe.getIngredients()),
                        escapeCsv(recipe.getDescription()),
                        escapeCsv(recipe.getLink())));
            }
        } catch (IOException e) {
            throw new RuntimeException("Помилка під час запису CSV", e);
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\""); // подвоїти лапки
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }


    // 3. Поиск по названию
    public List<Recipe> searchByTitle(String keyword) {
        return recipeRepository.findByTitleContainingIgnoreCase(keyword);
    }

    // 4. Поиск по ингредиенту
    public List<Recipe> searchByIngredient(String keyword) {
        return recipeRepository.findAll().stream()
                .filter(recipe -> {
                    String ingredients = recipe.getIngredients();
                    return ingredients != null && ingredients.toLowerCase().contains(keyword.toLowerCase());
                })
                .toList();
    }


    // 5. Фильтрация по категории (избранное)
    public List<Recipe> filterByCategory(String category) {
        return recipeRepository.findByCategoryIgnoreCase(category);
    }

    // 6. Добавить в избранное
    public void addToFavorites(Long recipeId) {
        Optional<Recipe> optional = recipeRepository.findById(recipeId);
        optional.ifPresent(recipe -> {
            recipe.setFavorite(true);
            recipeRepository.save(recipe);
        });
    }


    // 7. Удалить из избранного
    public void removeFromFavorites(Long recipeId) {
        recipeRepository.findById(recipeId).ifPresent(recipe -> {
            recipe.setFavorite(false);
            recipeRepository.save(recipe);
        });
    }


    // 8. Получить все избранные
    public List<Recipe> getFavorites() {
        return recipeRepository.findByFavoriteTrue();
    }


    // 9. Создание рецепта
    public void createRecipe(Recipe recipe) {
        recipeRepository.save(recipe);
    }

    // 10. Получить рецепт по ID
    public Recipe getRecipeById(Long id) {
        return recipeRepository.findById(id).orElse(null);
    }

    // 11. Обновить рецепт
    public void updateRecipe(Recipe recipe) {
        recipeRepository.save(recipe);
    }

    // 12. Удалить рецепт
    public void deleteRecipe(Long id) {
        recipeRepository.deleteById(id);
    }

    public List<Recipe> getAll() {
        return recipeRepository.findAll();
    }

}

