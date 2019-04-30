package com.bcs.core.utils;

import java.util.ArrayList;
import java.util.List;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActorContext;
import akka.routing.ActorRefRoutee;
import akka.routing.DefaultResizer;
import akka.routing.RoundRobinPool;
import akka.routing.Routee;
import akka.routing.Router;
import akka.routing.SmallestMailboxRoutingLogic;

import com.bcs.core.resource.CoreConfigReader;

public class AkkaRouterFactory<T>{

    public Router router;
    
    public ActorRef routerActor;
    
    private int lowerBound = 5;
    private DefaultResizer resizer;

	public AkkaRouterFactory(UntypedActorContext context, Class<T> targetClass){
		int size = 10;
		try{
			size = CoreConfigReader.getInteger("akka.router.size");
		}
		catch( Exception e){}
		this.createRouter(context, targetClass, size, false);
	}
	
	public AkkaRouterFactory(UntypedActorContext context, Class<T> targetClass, int size){
		this.createRouter(context, targetClass, size, false);
	}
	
	public AkkaRouterFactory(UntypedActorContext context, Class<T> targetClass, boolean isRrouterActor){
		int size = 10;
		try{
			size = CoreConfigReader.getInteger("akka.router.size");
		}
		catch( Exception e){}
		this.createRouter(context, targetClass, size, isRrouterActor);
	}
	
	public AkkaRouterFactory(UntypedActorContext context, Class<T> targetClass, int size, boolean isRrouterActor){
		this.createRouter(context, targetClass, size, isRrouterActor);
	}
	
	private void createRouter(UntypedActorContext context, Class<T> targetClass, int size, boolean isRrouterActor){

		if(isRrouterActor){
			resizer = new DefaultResizer(lowerBound, size);
			
			routerActor = context.actorOf(new RoundRobinPool(size).withResizer(resizer).props( Props.create(targetClass)), "routerActor" + this.toString());
		}
		else{
			
			List<Routee> routees = new ArrayList<Routee>();
		    for (int i = 0; i < 20; i++) {
		      ActorRef r = context.actorOf(Props.create(targetClass));
		      context.watch(r);
		      routees.add(new ActorRefRoutee(r));
		    }
		    router =  new Router(new SmallestMailboxRoutingLogic(), routees);
		}
	}
}
