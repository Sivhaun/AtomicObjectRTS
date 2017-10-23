package com.atomicobject.rts;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class Client {
	
	BufferedReader input;
	OutputStreamWriter out;
	LinkedBlockingQueue<Map<String, Object>> updates;
	Map<Long, Unit> units;
	Board board;
	long turn;

	public Client(Socket socket) {
		updates = new LinkedBlockingQueue<Map<String, Object>>();
		units = new HashMap<Long, Unit>();
		try {
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new OutputStreamWriter(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void start() {
		System.out.println("Starting client threads ...");
		new Thread(() -> readUpdatesFromServer()).start();
		new Thread(() -> runClientLoop()).start();
	}
	
	public void readUpdatesFromServer() {
		String nextLine;
		try {
			while ((nextLine = input.readLine()) != null) {
				@SuppressWarnings("unchecked")
				Map<String, Object> update = (Map<String, Object>) JSONValue.parse(nextLine.trim());
				updates.add(update);
			}
		} catch (IOException e) {
			// exit thread
		}		
	}

	public void runClientLoop() {
		System.out.println("Starting client update/command processing ...");
		try {
			while (true) {
				processUpdateFromServer();
				respondWithCommands();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		closeStreams();
	}

	private void processUpdateFromServer() throws InterruptedException {
		Map<String, Object> update = updates.take();
		if (update != null) {
			System.out.println("Processing udpate: " + update);
			@SuppressWarnings("unchecked")
			Collection<JSONObject> unitUpdates = (Collection<JSONObject>) update.get("unit_updates");
			turn = (long) update.get("turn");

			if (turn == 0) {
				org.json.simple.JSONObject info = (org.json.simple.JSONObject)update.get("game_info");

				long width = (long)info.get("map_width");
				long height =(long)info.get("map_height");
				board = new Board(width, height);
			}


			Collection<JSONObject> tileUpdates = (Collection<JSONObject>) update.get("tile_updates");
			processTileUpdates(tileUpdates);

			addUnitUpdate(unitUpdates);

		}
	}

	private void processTileUpdates(Collection<JSONObject> tileUpdates){
		for( JSONObject Tupdate: tileUpdates) {
			if ((boolean)(Tupdate.get("visible"))== true)
			{
				Tile tile = new Tile(Tupdate);
				board.put(tile);
			}
		}

	}

	private void addUnitUpdate(Collection<JSONObject> unitUpdates) {
		unitUpdates.forEach((unitUpdate) -> {
			Long id = (Long) unitUpdate.get("id");
			String type = (String) unitUpdate.get("type");
			if (!type.equals("base")) {
				units.put(id, new Unit(unitUpdate));
			}
		});
	}

	private void respondWithCommands() throws IOException {
		if (units.size() == 0) return;
		
		JSONArray commands = buildCommandList();		
		sendCommandListToServer(commands);
	}

	@SuppressWarnings("unchecked")
	private JSONArray buildCommandList() {

		Long[] unitIds = units.keySet().toArray(new Long[units.size()]);
		JSONArray commands = new JSONArray();

		for (int i = 0; i < unitIds.length; i++) {
			JSONObject command = new JSONObject();
			Long unitId = unitIds[(int)i];
			determineCommand(command, units, unitId);
			commands.add(command);
		}
		return commands;
	}

	private void determineCommand(JSONObject command,Map units,long unitId){
		String[] directions = {"N","E","S","W"};
		String direction = directions[(int) Math.floor(Math.random() * 4)];
		Unit unit = (Unit)units.get(unitId);
		Location start = new Location(unit.x, unit.y);
		Location target;


		if (unit.resource > 0 )
		{
			System.out.println(unit.resource);
			direction = board.pathFinding(start, new Location(0, 0));
			command.put("command", "Move");
			command.put("dir", direction);
			command.put("unit", unitId);
		}



		String nextTo = board.isNexToRecource(start);
		if (!nextTo.equalsIgnoreCase("null")){
			direction = nextTo;
			command.put("command", "GATHER");
			command.put("dir", direction);
			command.put("unit", unitId);

		}


		else if (board.recourceTiles.size()>2){
			target = board.nearestResocurce(board.get(start));
			System.out.println("start: " + start + "target " + target);
			direction = board.pathFinding(start, target);
			System.out.println((direction));
		}

		command.put("command", "MOVE");
		command.put("dir", direction);
		command.put("unit", unitId);

	}

	@SuppressWarnings("unchecked")
	private void sendCommandListToServer(JSONArray commands) throws IOException {
		JSONObject container = new JSONObject();
		container.put("commands", commands);
		System.out.println("Sending commands: " + container.toJSONString());
		out.write(container.toJSONString());
		out.write("\n");
		out.flush();
	}

	private void closeStreams() {
		closeQuietly(input);
		closeQuietly(out);
	}

	private void closeQuietly(Closeable stream) {
		try {
			stream.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
