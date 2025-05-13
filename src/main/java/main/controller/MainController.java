package main.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import main.entities.Recipe;
import main.repository.RecipeRepository;
import main.service.MainService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Controller
@RequiredArgsConstructor
@RequestMapping("/")
public class MainController {

    private final MainService mainService;
    private final RecipeRepository recipeRepository;

    // Главная страница
    @GetMapping
    public String index() {
        return "index";
    }

    // Страница со всеми рецептами
    @GetMapping("/recipes")
    public String recipesPage(Model model) {
        model.addAttribute("recipes", mainService.getAll());
        return "recipes";
    }

    // Создание нового рецепта
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("recipe", new Recipe());
        return "create_recipe";
    }

    @PostMapping("/save")
    public String saveRecipe(@ModelAttribute Recipe recipe) {
        recipeRepository.save(recipe);
        return "redirect:/recipes";
    }

    // Редактирование
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Нет рецепта с id: " + id));
        model.addAttribute("recipe", recipe);
        return "edit_recipe";
    }

    // Удаление рецепта
    @PostMapping("/delete/{id}")
    public String deleteRecipe(@PathVariable Long id) {
        recipeRepository.deleteById(id);
        return "redirect:/recipes";
    }

    // Избранные рецепты
    @GetMapping("/favorites")
    public String favoritesPage(Model model) {
        model.addAttribute("recipes", mainService.getFavorites());
        return "favorites";
    }

    // Поиск по названию
    @GetMapping("/search/title")
    public String searchByTitle(@RequestParam String keyword, Model model) {
        model.addAttribute("recipes", mainService.searchByTitle(keyword));
        return "recipes";
    }

    // Поиск по ингредиентам
    @GetMapping("/search/ingredient")
    public String searchByIngredient(@RequestParam String keyword, Model model) {
        List<Recipe> recipes = recipeRepository.findByIngredientContainingIgnoreCase(keyword);
        model.addAttribute("recipes", recipes);
        return "recipes";
    }


    // Фильтрация по категории
    @GetMapping("/category")
    public String filterByCategory(@RequestParam String category, Model model) {
        model.addAttribute("recipes", mainService.filterByCategory(category));
        return "recipes";
    }

    // Добавление в избранное
    @PostMapping("/add-favorites/{id}")
    public String addFavorite(@PathVariable Long id) {
        mainService.addToFavorites(id);
        return "redirect:/recipes";
    }

    // Массовые действия: добавить в избранное или удалить
    @PostMapping("/batch-action")
    public String handleBatchAction(
            @RequestParam(name = "recipeIds", required = false) List<Long> recipeIds,
            @RequestParam String action) {

        if (recipeIds != null && !recipeIds.isEmpty()) {
            switch (action) {
                case "delete":
                    recipeIds.forEach(recipeRepository::deleteById);
                    break;
                case "favorite":
                    recipeIds.forEach(mainService::addToFavorites);
                    break;
            }
        }
        return "redirect:/recipes";
    }


    @PostMapping("/favorite-action")
    public String removeFavorites(
            @RequestParam(name = "recipeIds_2", required = false) List<Long> recipeIds,
            @RequestParam String action) {

        if (recipeIds != null || Objects.equals(action, "remove-favorite")) {
            for (Long id : recipeIds) {
                mainService.removeFromFavorites(id);
            }
        }
        return "redirect:/favorites";
    }

    // Импорт/экспорт
    @GetMapping("/import-form")
    public String showImportExportPage() {
        return "import_export";
    }

    @PostMapping("/import")
    public String importCsv(@RequestParam("file") MultipartFile file, Model model) {
        try {
            mainService.importFromCsv(file.getInputStream());
        } catch (IOException e) {
            model.addAttribute("error", "Ошибка при импорте: " + e.getMessage());
        }
        return "redirect:/recipes";
    }

    @GetMapping("/export")
    public void exportCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=recipes.csv");
        mainService.exportToCsv(response.getOutputStream());
    }
}
