package eu.virtusdevelops.simplemachines.data;

import eu.virtusdevelops.simplemachines.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.inventory.*;

import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class CrafterMachine extends BaseMachine{

    private static final Map<ItemStack, Recipes> cachedRecipes = new ConcurrentHashMap<>();

    ItemStack tool;


    public CrafterMachine(int id, UUID placedBy, MachineLocation location, int size,
                          int max_size, int fuel, int max_fuel, int speed, int max_speed, ItemStack tool, String config_name, int actual_speed){

        super(id, placedBy, location, size, max_size, fuel, max_fuel, speed, max_speed, tool, config_name, MachineType.CRAFTER, actual_speed);
        setTool(tool);
        //this.tool = tool;
    }


    @Override
    public void setTool(ItemStack itemStack){
        this.tool = itemStack;
        cachedRecipes.clear();
        getRecipes(itemStack);

    }




    @Override
    public ItemStack getTool(){
        return this.tool;
    }




    @Override
    public void tick(int tick) {
        if(tick % getActual_speed() != 0) return;
        if(!isChunkLoaded()) return;
        Block block = getLocation().getBukkitLocation().getBlock();
        if(getFuel() < 1) return;


        BlockData data = block.getBlockData();

        if(data instanceof Directional){
            BlockFace face = ((Directional) data).getFacing();

            Block frontBlock =  block.getRelative(face);
            Block backBlock = block.getRelative(face.getOppositeFace());

            if(frontBlock.getType() == Material.CHEST && backBlock.getType() == Material.CHEST){
               if(frontBlock.getState() instanceof Chest chest && backBlock.getState() instanceof Chest backChest){

                   //tryCraft(chest, backChest);
                   if(tryCraft(chest, backChest)){
                       removeFuel(1);
                   }
               }
            }
        }
    }

    private ItemStack[] cloneInventory(Chest chest){
        ItemStack[] itemsold = chest.getInventory().getContents();
        ItemStack[] items = new ItemStack[itemsold.length];

        for(int i = 0; i < chest.getInventory().getContents().length; i++){
            if(itemsold[i] != null){
                items[i] = itemsold[i].clone();
            }
        }

        return items;
    }


    private boolean tryCraft(Chest chest, Chest backChest){

        ItemStack[]  items = cloneInventory(chest); // chest.getInventory().getContents().clone(); // change this and actually clone the inventory.
        ItemStack[] items2 = backChest.getInventory().getContents();

        boolean crafted = false;

        recipeLoop:
        for(SimpleRecipe recipe : getRecipes(tool).recipes){
            Map<Integer, Integer> slotsToAlter = new HashMap<>();


            for (SimpleRecipe.SimpleIngredient ingredient : recipe.ingredients) {
                int amount = ingredient.item.getAmount() + ingredient.getAdditionalAmount();

                for (int i = 0; i < items.length; i++) {
                    ItemStack item = items[i];

                    if (item == null) continue;
                    if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) continue;

                    boolean sameMaterial = Methods.isSimilarMaterial(item, ingredient.item);

                    // Check if any alternative Material matches
                    if (!sameMaterial) {
                        for (ItemStack alternativeType : ingredient.alternativeTypes) {
                            if (Methods.isSimilarMaterial(item, alternativeType)) {
                                sameMaterial = true;
                                break;
                            }
                        }

                        // Still doesn't not match --> Skip this item
                        if (!sameMaterial) continue;
                    }

                    if (item.getAmount() >= amount) {
                        slotsToAlter.put(i, item.getAmount() - amount);
                        amount = 0;
                    } else {
                        slotsToAlter.put(i, 0);
                        amount -= item.getAmount();
                    }
                }

                // Not enough ingredients for this recipe
                if (amount != 0) continue recipeLoop;
            }


            for (Map.Entry<Integer, Integer> entry : slotsToAlter.entrySet()) {
                if (entry.getValue() <= 0) {
                    items[entry.getKey()] = null;
                } else {
                    items[entry.getKey()].setAmount(entry.getValue());
                }
            }

            if(Methods.hasSpace(backChest.getInventory(), recipe.result)) {
                for (int i = 0; i < items2.length; i++) {
                    if (items2[i] == null ||
                            (items2[i].isSimilar(recipe.result)
                                    && items2[i].getAmount() + recipe.result.getAmount() <= items2[i].getMaxStackSize())) {
                        if (items2[i] == null) {
                            items2[i] = recipe.result.clone();
                        } else {
                            items2[i].setAmount(items2[i].getAmount() + recipe.result.getAmount());
                        }

                        break;
                    }

                }
                crafted = true;
                backChest.getInventory().setContents(items2);
                chest.getInventory().setContents(items);
            }
        }
        return crafted;
    }



    private Recipes getRecipes(ItemStack toCraft) {
        Recipes recipes = cachedRecipes.get(toCraft);
        if (recipes == null) {
            try {
                recipes = new Recipes(Bukkit.getServer().getRecipesFor(toCraft));
            } catch (Throwable t) {
                // extremely rare, but y'know - some plugins are dumb
                recipes = new Recipes();
                // how's about we try this manually?
                java.util.Iterator<Recipe> recipeIterator = Bukkit.getServer().recipeIterator();
                while (recipeIterator.hasNext()) {
                    try {
                        Recipe recipe = recipeIterator.next();

                        ItemStack stack = recipe.getResult();
                        if (Methods.isSimilarMaterial(stack, toCraft))
                            recipes.addRecipe(recipe);
                    } catch (Throwable ignored) {
                    }
                }
            }
            cachedRecipes.put(toCraft, recipes);
        }
        return recipes;
    }



    private final static class Recipes {
        private final List<SimpleRecipe> recipes = new ArrayList<>();
        // Used for the blacklist to ensure that items are not going to get transferred
        private final List<Material> possibleIngredientTypes = new ArrayList<>();

        public Recipes() {
        }

        public Recipes(Collection<Recipe> recipes) {
            addRecipes(recipes);
        }

        public List<SimpleRecipe> getRecipes() {
            return Collections.unmodifiableList(recipes);
        }

        public List<Material> getPossibleIngredientTypes() {
            return Collections.unmodifiableList(possibleIngredientTypes);
        }

        public void addRecipe(Recipe recipe) {
            SimpleRecipe simpleRecipe = null;

            if (recipe instanceof ShapelessRecipe) {
                simpleRecipe = new SimpleRecipe((ShapelessRecipe) recipe);
            } else if (recipe instanceof ShapedRecipe) {
                simpleRecipe = new SimpleRecipe((ShapedRecipe) recipe);
            } else if(recipe instanceof FurnaceRecipe){
                simpleRecipe = new SimpleRecipe((FurnaceRecipe) recipe);
            }

            // Skip unsupported recipe type
            if (simpleRecipe == null) return;

            this.recipes.add(simpleRecipe);

            // Keep a list of all possible ingredients.
            for (SimpleRecipe.SimpleIngredient ingredient : simpleRecipe.ingredients) {
                if (!possibleIngredientTypes.contains(ingredient.item.getType())) {
                    possibleIngredientTypes.add(ingredient.item.getType());
                }

                for (ItemStack material : ingredient.alternativeTypes) {
                    if (!possibleIngredientTypes.contains(material.getType())) {
                        possibleIngredientTypes.add(material.getType());
                    }
                }
            }
        }

        public void addRecipes(Collection<Recipe> recipes) {
            recipes.forEach(this::addRecipe);
        }

        public boolean hasRecipes() {
            return !recipes.isEmpty();
        }

        public void clearRecipes() {
            recipes.clear();
        }
    }


    private final static class SimpleRecipe {
        private final SimpleIngredient[] ingredients;
        private final ItemStack result;

        public SimpleRecipe(ShapelessRecipe recipe) {
            this.result = recipe.getResult();

            List<SimpleIngredient> ingredients = new ArrayList<>();

            for (int i = 0; i < recipe.getIngredientList().size(); i++) {
                ItemStack item = recipe.getIngredientList().get(i);
                RecipeChoice rChoice = null;

                try {
                    rChoice = recipe.getChoiceList().get(i);
                } catch (NoSuchMethodError ignore) {    // Method missing in Spigot 1.12.2
                }

                processIngredient(ingredients, item, rChoice);
            }

            this.ingredients = ingredients.toArray(new SimpleIngredient[0]);
        }

        public SimpleRecipe(FurnaceRecipe recipe){
            result = recipe.getResult();
            List<SimpleIngredient> ingredients = new ArrayList<>();
            ingredients.add(new SimpleIngredient(recipe.getInput(), List.of()));
            this.ingredients = ingredients.toArray(new SimpleIngredient[0]);
        }

        public SimpleRecipe(ShapedRecipe recipe) {
            this.result = recipe.getResult();

            List<SimpleIngredient> ingredients = new ArrayList<>();

            for (Map.Entry<Character, ItemStack> entry : recipe.getIngredientMap().entrySet()) {
                ItemStack item = entry.getValue();
                RecipeChoice rChoice = null;

                try {
                    rChoice = recipe.getChoiceMap().get(entry.getKey());
                } catch (NoSuchMethodError ignore) {    // Method missing in Spigot 1.12.2
                }

                if (item == null) continue;

                processIngredient(ingredients, item, rChoice);
            }

            this.ingredients = ingredients.toArray(new SimpleIngredient[0]);
        }

        private void processIngredient(List<SimpleIngredient> ingredients, ItemStack item, RecipeChoice rChoice) {
            List<Material> alternativeTypes = new LinkedList<>();

            if (rChoice instanceof RecipeChoice.MaterialChoice) {
                for (Material possType : ((RecipeChoice.MaterialChoice) rChoice).getChoices()) {
                    if (item.getType() != possType) {
                        alternativeTypes.add(possType);
                    }
                }
            }

            SimpleIngredient simpleIngredient = new SimpleIngredient(item, alternativeTypes);

            // Search for existing ingredients
            for (SimpleIngredient ingredient : ingredients) {
                if (ingredient.isSimilar(simpleIngredient)) {
                    ingredient.addAdditionalAmount(item.getAmount());
                    simpleIngredient = null;
                    break;
                }
            }

            // No existing ingredient found?
            if (simpleIngredient != null) {
                ingredients.add(simpleIngredient);
            }
        }

        private static class SimpleIngredient {
            private final ItemStack item;
            private final ItemStack[] alternativeTypes;

            /**
             * <b>Ignored by {@link #isSimilar(Object)}!</b><br>
             * This amount should be added to {@link #item} when crafting,
             * to consider the complete item costs
             */
            private int additionalAmount = 0;

            /**
             * @throws NullPointerException If any of the parameters is null
             */
            SimpleIngredient(ItemStack item, List<Material> alternativeTypes) {
                Objects.requireNonNull(item);
                Objects.requireNonNull(alternativeTypes);

                this.item = item;

                this.alternativeTypes = new ItemStack[alternativeTypes.size()];

                for (int i = 0; i < alternativeTypes.size(); i++) {
                    this.alternativeTypes[i] = this.item.clone();
                    this.alternativeTypes[i].setType(alternativeTypes.get(i));
                }
            }

            public int getAdditionalAmount() {
                return additionalAmount;
            }

            public void addAdditionalAmount(int amountToAdd) {
                additionalAmount += amountToAdd;
            }

            /**
             * Like {@link #equals(Object)} but ignores {@link #additionalAmount} and {@link ItemStack#getAmount()}
             *
             * @return If two {@link SimpleIngredient} objects are equal
             *         while ignoring any item amounts, true otherwise false
             */
            public boolean isSimilar(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                SimpleIngredient that = (SimpleIngredient) o;
                return item.isSimilar(that.item) &&
                        Arrays.equals(alternativeTypes, that.alternativeTypes);
            }
        }
    }


}
