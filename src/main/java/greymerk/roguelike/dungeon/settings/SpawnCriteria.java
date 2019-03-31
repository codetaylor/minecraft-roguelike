package greymerk.roguelike.dungeon.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.BiomeDictionary;

public class SpawnCriteria {

	int weight;
	List<Integer> whitelist;
	List<Integer> blacklist;
	List<ResourceLocation> biomes;
	List<BiomeDictionary.Type> biomeTypes;
	boolean everywhere;

	public SpawnCriteria(){
		this.weight = 1;
		this.whitelist = new ArrayList<Integer>();
		this.blacklist = new ArrayList<Integer>();
		this.biomes = new ArrayList<ResourceLocation>();
		this.biomeTypes = new ArrayList<BiomeDictionary.Type>();
		this.everywhere = false;
	}

	public SpawnCriteria(JsonObject data){
		this();

		if (data.has("dimensionWL")) {
			JsonArray whitelist = data.get("dimensionWL").getAsJsonArray();
			this.whitelist = new ArrayList<Integer>();
			for (JsonElement e : whitelist) {
				int id = e.getAsInt();
				this.whitelist.add(id);
			}
		}

		if (data.has("dimensionBL")) {
			JsonArray blacklist = data.get("dimensionBL").getAsJsonArray();
			this.blacklist = new ArrayList<Integer>();
			for (JsonElement e : blacklist) {
				int id = e.getAsInt();
				this.blacklist.add(id);
			}
		}


		this.weight = data.has("weight") ? data.get("weight").getAsInt() : 1;

		if(data.has("biomes")){
			JsonArray biomeList = data.get("biomes").getAsJsonArray();
			this.biomes = new ArrayList<ResourceLocation>();
			for(JsonElement e : biomeList){
				String name = e.getAsString();
				this.biomes.add(new ResourceLocation(name));
			}
		}

		if(data.has("biomeTypes")){
			JsonArray biomeTypeList = data.get("biomeTypes").getAsJsonArray();
			this.biomeTypes = new ArrayList<BiomeDictionary.Type>();
			for(JsonElement e : biomeTypeList){
				String type = e.getAsString().toUpperCase();
				BiomeDictionary.Type t = BiomeDictionary.Type.getType(type, new BiomeDictionary.Type[0]);
				if(BiomeDictionary.getBiomes(t).size() > 0) this.biomeTypes.add(t);
			}
		}

		this.everywhere = this.isEverywhere();
	}

	public void setDimensionWhitelist(List<Integer> whitelist) {

		this.whitelist = whitelist;
		this.everywhere = this.isEverywhere();
	}

	public void setDimensionBlacklist(List<Integer> blacklist) {

		this.blacklist = blacklist;
		this.everywhere = this.isEverywhere();
	}

	public void setWeight(int weight){
		this.weight = weight;
	}

	public void setbiomes(List<ResourceLocation> biomes){
		this.biomes = biomes;
		this.everywhere = this.isEverywhere();
	}

	public void setBiomeTypes(List<BiomeDictionary.Type> biomeTypes){
		this.biomeTypes = biomeTypes;
		this.everywhere = this.isEverywhere();
	}

	private boolean isEverywhere() {

		return this.biomes.isEmpty()
				&& this.biomeTypes.isEmpty()
				&& this.whitelist.isEmpty()
				&& this.blacklist.isEmpty();
	}

	public boolean isValid(ISpawnContext context){

		// If no biomes, biome types, whitelist, or blacklist is supplied, return early.
		if(this.everywhere) return true;

		// Return false on invalid dimension.
		int dimension = context.getDimension();
		if(!SpawnCriteria.isValidDimension(dimension, this.whitelist, this.blacklist)) return false;

		boolean hasBiomeWL = this.biomes != null && !this.biomes.isEmpty();
		boolean hasBiomeTypeWL = this.biomeTypes != null && !this.biomeTypes.isEmpty();

		if (hasBiomeWL || hasBiomeTypeWL) {

			// If a biome WL is provided, check it.
			if (hasBiomeWL && context.includesBiome(this.biomes)) {
				return true;
			}

			// If a biome type WL is provided, check it.
			if (hasBiomeTypeWL) {
				return context.includesBiomeType(this.biomeTypes);
			}

			return false;

		} else {
			// No biome whitelists provided.
			return true;
		}
	}

	public static boolean isValidDimension(int dim, List<Integer> wl, List<Integer> bl){
		if(bl.contains(dim)) return false;
		if(wl.isEmpty()) return true;
		return wl.contains(dim);
	}

}