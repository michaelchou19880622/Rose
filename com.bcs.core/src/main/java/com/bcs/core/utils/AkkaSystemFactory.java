package com.bcs.core.utils;

import java.util.List;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import com.bcs.core.resource.CoreConfigReader;

public class AkkaSystemFactory<T>{

	public AkkaSystemFactory(List<ActorSystem> systems, List<ActorRef> masters, Class<T> targetClass, String systemName, String masterName){
		int size = 5;
		try{
			size = CoreConfigReader.getInteger("akka.system.size");
		}
		catch( Exception e){}
		this.createListSystem(size, systems, masters, targetClass, systemName, masterName);
	}
	
	public AkkaSystemFactory(int size, List<ActorSystem> systems, List<ActorRef> masters, Class<T> targetClass, String systemName, String masterName){
		this.createListSystem(size, systems, masters, targetClass, systemName, masterName);
	}
	
	private void createListSystem(int size, List<ActorSystem> systems, List<ActorRef> masters, Class<T> targetClass, String systemName, String masterName){

		for(int i = 0; i < size; i++){
			ActorSystem system = ActorSystem.create(systemName + i);
			systems.add(system);
			
			masters.add(system.actorOf(Props.create(targetClass), masterName));
		}
	}
}
