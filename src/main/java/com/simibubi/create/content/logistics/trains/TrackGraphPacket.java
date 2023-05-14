package com.simibubi.create.content.logistics.trains;

import java.util.UUID;

import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.networking.SimplePacketBase;

public abstract class TrackGraphPacket extends SimplePacketBase {

	public UUID graphId;
	public int netId;
	public boolean packetDeletesGraph;

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> handle(CreateClient.RAILWAYS, CreateClient.RAILWAYS.getOrCreateGraph(graphId, netId)));
		return true;
	}

	protected abstract void handle(GlobalRailwayManager manager, TrackGraph graph);

}
