package rmi;

import java.net.InetSocketAddress;

public class NotifySkeleton<T> extends Skeleton<T> {

	public NotifySkeleton(Class<T> c, T server, InetSocketAddress address) {
		super(c, server, address);
	}

	@Override
	protected void stopped(Throwable cause){
		synchronized(this){
			this.notify();
		}
	}

}
