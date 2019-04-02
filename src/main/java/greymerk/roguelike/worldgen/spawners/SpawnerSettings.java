package greymerk.roguelike.worldgen.spawners;

import java.util.*;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import greymerk.roguelike.util.WeightedChoice;
import greymerk.roguelike.util.WeightedRandomizer;
import greymerk.roguelike.worldgen.Coord;
import greymerk.roguelike.worldgen.IWorldEditor;


public class SpawnerSettings {

	private Map<Spawner, WeightedRandomizer<Spawnable>> spawners;
	
	public SpawnerSettings(){
		this.spawners = new HashMap<Spawner, WeightedRandomizer<Spawnable>>();
	}
	
	@Deprecated
	public SpawnerSettings(JsonObject data) throws Exception{
		throw new Exception("Not implemented");
	}
	
	public SpawnerSettings(SpawnerSettings toCopy){
		this();
		for(Spawner type : toCopy.spawners.keySet()){
			if(this.spawners.get(type) == null){
				this.spawners.put(type, new WeightedRandomizer<Spawnable>());
			}
			this.spawners.get(type).merge(toCopy.spawners.get(type));
		}
	}
	
	public SpawnerSettings(SpawnerSettings base, SpawnerSettings other){
		this();

		for(Spawner type : base.spawners.keySet()){
			if(this.spawners.get(type) == null){
				this.spawners.put(type, new WeightedRandomizer<Spawnable>());
			}
			this.spawners.get(type).merge(base.spawners.get(type));
		}
		
		for(Spawner type : other.spawners.keySet()){
			if(this.spawners.get(type) == null){
				this.spawners.put(type, new WeightedRandomizer<Spawnable>());
			}
			this.spawners.get(type).merge(other.spawners.get(type));
		}
	}
	
	public void add(JsonObject entry) throws Exception{

		List<Spawner> spawnerTypeList = new ArrayList<>(Spawner.values().length);

		if(!entry.has("type")) {
			// If the spawner object is missing a type element, add to all types...
			spawnerTypeList.addAll(Arrays.asList(Spawner.values()));
		} else {
			// Else, gather types from array or string...
			JsonElement typeElement = entry.get("type");
			if (typeElement.isJsonArray()) {
				for (JsonElement jsonElement : typeElement.getAsJsonArray()) {
					String typeName = jsonElement.getAsString().toUpperCase();
					try {
						Spawner type = Spawner.valueOf(typeName);
						spawnerTypeList.add(type);
					} catch (Exception e) {
						throw new Exception("No such Spawner type: " + typeName, e);
					}
				}
			} else {
				String typeName = typeElement.getAsString().toUpperCase();
				try {
					Spawner type = Spawner.valueOf(typeName);
					spawnerTypeList.add(type);
				} catch (Exception e) {
					throw new Exception("No such Spawner type: " + typeElement.getAsString(), e);
				}
			}
		}

		JsonElement potentials = entry.get("potentials");
		Spawnable spawn = new Spawnable(potentials);
		
		int weight = entry.has("weight") ? entry.get("weight").getAsInt() : 1;

		// Add the spawner to all gathered types.
		for (Spawner type : spawnerTypeList) {
			if(!this.spawners.containsKey(type)){
				this.spawners.put(type, new WeightedRandomizer<Spawnable>());
			}

			WeightedChoice<Spawnable> toAdd = new WeightedChoice<Spawnable>(spawn, weight);
			this.spawners.get(type).add(toAdd);
		}
	}

	public void generate(IWorldEditor editor, Random rand, Coord cursor, Spawner type, int level){
		Spawnable toSpawn = spawners.containsKey(type) ? spawners.get(type).get(rand) : new Spawnable(type);
		toSpawn.generate(editor, rand, cursor, level);
	}
	
	@Override
	public String toString(){
		return this.spawners.keySet().toString();
	}
}
