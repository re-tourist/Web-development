// 初始化：获取 DOM 元素
const searchInput = document.getElementById("search-input");
const searchBtn = document.getElementById("search-btn");
const mealsContainer = document.getElementById("meals");
const resultHeading = document.getElementById("result-heading");
const errorContainer = document.getElementById("error-container");
const mealDetails = document.getElementById("meal-details");
const mealDetailsContent = document.querySelector(".meal-details-content");
const backBtn = document.getElementById("back-btn");

// TheMealDB 接口
const BASE_URL = "https://www.themealdb.com/api/json/v1/1/";
const SEARCH_URL = `${BASE_URL}search.php?s=`; // 根据菜名搜索
const LOOKUP_URL = `${BASE_URL}lookup.php?i=`; // 根据菜谱ID查看详情

// 辅助：渲染错误信息
function showError(msg) {
  errorContainer.textContent = msg;
  errorContainer.classList.remove("hidden");
}
function clearError() {
  errorContainer.textContent = "";
  errorContainer.classList.add("hidden");
}

// 搜索菜单
async function searchMeals() {
  const searchTerm = searchInput.value.trim();

  // 边界：空输入
  if (!searchTerm) {
    showError("Please enter a search term");
    return;
  }

  try {
    resultHeading.textContent = `Searching for "${searchTerm}"...`;
    mealsContainer.innerHTML = "";
    clearError();

    const response = await fetch(`${SEARCH_URL}${encodeURIComponent(searchTerm)}`);
    const data = await response.json();

    if (!data || data.meals === null) {
      resultHeading.textContent = "";
      mealsContainer.innerHTML = "";
      showError(`No recipes found for "${searchTerm}". Try another search term!`);
    } else {
      resultHeading.textContent = `Search results for "${searchTerm}":`;
      displayMeals(data.meals);
      // 保存最近一次搜索关键词与结果到 localStorage
      try {
        localStorage.setItem("p5_last_search", searchTerm);
        localStorage.setItem(
          "p5_last_results",
          JSON.stringify(
            data.meals.map((m) => ({
              idMeal: m.idMeal,
              strMeal: m.strMeal,
              strMealThumb: m.strMealThumb,
              strCategory: m.strCategory || ""
            }))
          )
        );
        localStorage.setItem("p5_last_saved_at", String(Date.now()));
      } catch (_) {}
      searchInput.value = "";
    }
  } catch (err) {
    showError("Something went wrong. Please try again later.");
  }
}

// 渲染菜单列表
function displayMeals(meals) {
  mealsContainer.innerHTML = "";
  meals.forEach((meal) => {
    mealsContainer.innerHTML += `
      <div class="meal" data-meal-id="${meal.idMeal}">
        <img src="${meal.strMealThumb}" alt="${meal.strMeal}">
        <div class="meal-info">
          <h3 class="meal-title">${meal.strMeal}</h3>
          ${meal.strCategory ? `<div class="meal-category">${meal.strCategory}</div>` : ""}
        </div>
      </div>
    `;
  });
}

// 点击菜单项，查看详情
async function handleMealClick(e) {
  const mealEl = e.target.closest(".meal");
  if (!mealEl) return;

  const mealId = mealEl.getAttribute("data-meal-id");
  try {
    const response = await fetch(`${LOOKUP_URL}${encodeURIComponent(mealId)}`);
    const data = await response.json();

    if (data.meals && data.meals[0]) {
      const meal = data.meals[0];

      // 组装配料
      const ingredients = [];
      for (let i = 1; i <= 20; i++) {
        const ing = meal[`strIngredient${i}`];
        const mea = meal[`strMeasure${i}`];
        if (ing && ing.trim() !== "") {
          ingredients.push({ ingredient: ing, measure: mea || "" });
        }
      }

      // 渲染详情
      mealDetailsContent.innerHTML = `
        <img src="${meal.strMealThumb}" alt="${meal.strMeal}" class="meal-details-img">
        <h2 class="meal-details-title">${meal.strMeal}</h2>
        <div class="meal-details-category">
          <span>${meal.strCategory || "Uncategorized"}</span>
        </div>
        <div class="meal-details-instructions">
          <h3>Instructions</h3>
          <p>${meal.strInstructions || "No instructions provided."}</p>
        </div>
        <div class="meal-details-ingredients">
          <h3>Ingredients</h3>
          <ul class="ingredients-list">
            ${ingredients
              .map((item) => `<li><i class="fas fa-check"></i> ${item.measure} ${item.ingredient}</li>`) 
              .join("")}
          </ul>
        </div>
        ${meal.strYoutube ? `<a href="${meal.strYoutube}" target="_blank" class="youtube-link"><i class=\"fab fa-youtube\"></i> Watch Video</a>` : ""}
      `;
      mealDetails.classList.remove("hidden");
      mealDetails.setAttribute("aria-hidden", "false");
      mealDetails.scrollIntoView({ behavior: "smooth" });
    }
  } catch (err) {
    showError("Could not load recipe details. Please try again later.");
  }
}

// 返回按钮：隐藏详情
function hideDetails() {
  mealDetails.classList.add("hidden");
  mealDetails.setAttribute("aria-hidden", "true");
}

// 事件绑定
searchBtn.addEventListener("click", searchMeals);
searchInput.addEventListener("keypress", (e) => { if (e.key === "Enter") searchMeals(); });
mealsContainer.addEventListener("click", handleMealClick);
backBtn.addEventListener("click", hideDetails);

// 初始：若有最近一次搜索，自动显示
try {
  const last = localStorage.getItem("p5_last_search");
  if (last && last.trim()) {
    // 仅回填最近一次搜索，不自动触发搜索
    searchInput.value = last;
  }
} catch (_) {}