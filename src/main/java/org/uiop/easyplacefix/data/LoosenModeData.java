package org.uiop.easyplacefix.data;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.w3c.dom.ranges.Range;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.stream.Collectors;

public class  LoosenModeData {
//    static HashSet<Item> itemHashSet = new HashSet<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "loosenMode.json");
    private static final Type ITEM_SET_TYPE = new TypeToken<HashSet<Integer>>() {}.getType();
    public static  HashSet<Item> items =new HashSet<>();
static {
    loadFromFile();
}

    public static HashSet<ItemStack> loadFromFile() {
        if (CONFIG_FILE.exists()) {
            try (Reader reader = new FileReader(CONFIG_FILE)) {
                HashSet<Integer> itemIds = GSON.fromJson(reader, ITEM_SET_TYPE);
                items.clear();
                HashSet<ItemStack> itemStackHashSet = itemIds.stream()
                        .map( id -> {
                          Item item =  Item.byRawId(id);
                            items.add(item);
                            return item.getDefaultStack();})
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(HashSet::new));
                    return itemStackHashSet;
            } catch (IOException e) {
                System.err.println("Failed to load config file:");
                e.printStackTrace();
            }
        } else {
            saveToFile(new HashSet<>());
           // 创建新文件
        }
        return new HashSet<>();
    }

    public static void saveToFile(Collection<ItemStack> itemHashSet) {
        items.clear();
        HashSet<Integer> itemIds = itemHashSet.stream()
                .map(itemStack -> {
                    Item item =itemStack.getItem();
                    items.add(item);
                    return Item.getRawId(item);})
                .collect(Collectors.toCollection(HashSet::new));

        try (Writer writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(itemIds, writer);
        } catch (IOException e) {
            System.err.println("Failed to save config file:");
            e.printStackTrace();
        }
    }

}
