/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package readerjavawdemo;


 import java.net.DatagramSocket; 
import java.net.InetAddress;
 import java.net.InetSocketAddress; 
 import java.net.SocketAddress; 
 import java.nio.ByteBuffer; 
 import java.nio.CharBuffer; 
 import java.nio.channels.DatagramChannel; 
 import java.nio.channels.SelectionKey; 
 import java.nio.channels.Selector; 
 import java.nio.charset.Charset; 
 import java.util.Iterator; 
 import java.util.Set; 


/**
  * @author 徐辛波(sinpo.xu@hotmail.com) Oct 19, 2008
  */
 public class ReaderJavaWDemo extends Thread {
 	public void run() {
 		//TestOri();//测试原始的
            
            TestOrm();//
            
 	}
      
        public static void Bcd2AscEx(byte asc[], byte bcd[], int len)
        {
            int	i, j;
            int k;

            j = (len + len%2) / 2;
            k = 3*j;
            for(i=0; i<j; i++)
            {
                    asc[3*i]	= (byte)((bcd[i] >> 4) & 0x0f);
                    asc[3*i+1]	= (byte)(bcd[i] & 0x0f);
                    asc[3*i+2]	= 0x20;
            }
            for(i=0; i<k; i++)
            {
                    if ( (i+1) % 3 == 0 )
                    {
                            continue;
                    }
                    if( asc[i] > 0x09)
                    {
                            asc[i]	= (byte)(0x41 + asc[i] - 0x0a);
                    }
                    else	
                    {
                            asc[i]	+= 0x30;
                    }
            }

            asc[k] = 0;
            
        }
        
        public static void Bcd2AscEy(char asc[], char bcd[], int len)
        {
            int	i, j;
            int k;

            j = (len + len%2) / 2;
            k = 3*j;
            for(i=0; i<j; i++)
            {
                    asc[3*i]	= (char)((bcd[i] >> 4) & 0x0f);
                    asc[3*i+1]	= (char)(bcd[i] & 0x0f);
                    asc[3*i+2]	= 0x20;
            }
            for(i=0; i<k; i++)
            {
                    if ( (i+1) % 3 == 0 )
                    {
                            continue;
                    }
                    if( asc[i] > 0x09)
                    {
                            asc[i]	= (char)(0x41 + asc[i] - 0x0a);
                    }
                    else	
                    {
                            asc[i]	+= 0x30;
                    }
            }

            asc[k] = 0;
            
        }
        
        public static void TestOri(){
            
           Selector selector = null;
 		try {
 			DatagramChannel channel = DatagramChannel.open();
 			DatagramSocket socket = channel.socket();
 			channel.configureBlocking(false);
 			socket.bind(new InetSocketAddress(5057));
 
 			selector = Selector.open();
 			channel.register(selector, SelectionKey.OP_READ);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		ByteBuffer byteBuffer = ByteBuffer.allocate(65536);
                
 		//while (true) {
                if (true) {
 			try {
                            
                                byte chTemp[] = new byte[1024];
 				int eventsCount = selector.select(3000);
 				if (eventsCount > 0) {
 					Set selectedKeys = selector.selectedKeys();
 					Iterator iterator = selectedKeys.iterator();
 					while (iterator.hasNext()) {
 						SelectionKey sk = (SelectionKey) iterator.next();
 						iterator.remove();
 						if (sk.isReadable()) {
 							DatagramChannel datagramChannel = (DatagramChannel) sk
 									.channel();
 							SocketAddress sa = datagramChannel
 									.receive(byteBuffer);
 							byteBuffer.flip();
 
 							// 测试：通过将收到的ByteBuffer首先通过缺省的编码解码成CharBuffer 再输出马储油平台
 							//CharBuffer charBuffer = Charset.defaultCharset().decode(byteBuffer);
                                                        //ByteBuffer btBuffer = Charset.defaultCharset().decode(byteBuffer);
                                                        
                                                        Bcd2AscEx(chTemp, byteBuffer.array(), 8);
 							//System.out.println("receive message:"+ charBuffer.toString());
                                                        System.out.println("receive message:"+ String.valueOf(chTemp));
                                                        
 							byteBuffer.clear();
 
 							//String echo = "This is the reply message from 服务器。";
 							//ByteBuffer buffer = Charset.defaultCharset()
 							//		.encode(echo);
                                                        //chTemp = new char[2];
                                                        //chTemp[0] = 0x56;
                                                        //chTemp[1] = 0x78;
                                                        
                                                        //ByteBuffer buffer = ByteBuffer.allocate(chTemp.length);
                                                        //Charset cs = Charset.forName ("UTF-8");
                                                        //CharBuffer cb = CharBuffer.allocate (chTemp.length);
                                                        //cb.put (chTemp);
                                                        //cb.flip ();
                                                        
                                                        //ByteBuffer bb = cs.encode (cb);

                                                        
                                                        
                                                        //datagramChannel.send(bb, sa);
 							//datagramChannel.write(buffer);
 						}
 					}
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
                
                System.out.println("Program end!\n");
                 
        }
        
        
        
        //连接读写器测试
        public static void TestOrm(){
            
           Selector selector = null;
 		try {
 			DatagramChannel channel = DatagramChannel.open();
 			DatagramSocket socket = channel.socket();
 			channel.configureBlocking(false);
 			socket.bind(new InetSocketAddress(5057));
 
 			selector = Selector.open();
 			channel.register(selector, SelectionKey.OP_READ);
                        
                        
                        
                        //byte[] chTemp = new byte[] {0x40, 0x03, 0x0F, 0x00, (byte)0xAE};
                        //byte[] chTemp = new byte[] {0x40, 0x02, 0x02, (byte)0xBC};
                        byte[] chTemp = new byte[] {0x40, 0x02, 0x06, (byte)0xB8};
                        //chTemp[0] = 0x40;
                        //chTemp[1] = 0x03;
                        //chTemp[2] = 0x0F;
                        //chTemp[3] = 0x00;
                        //chTemp[4] = (byte)0xAE;

                        
            ByteBuffer bb = ByteBuffer.allocate (chTemp.length);
            bb.put (chTemp);
                        bb.flip ();
            



                        
                        
                        
                        //DatagramChannel ch= DatagramChannel.open();
 			//DatagramSocket so = ch.socket();
                        //ch.configureBlocking(false);
                        //SocketAddress sa = so.getLocalSocketAddress();
                        byte[] bs = new byte[] { (byte) 192, (byte) 168, 0, 71 }; 
                        InetAddress address=InetAddress.getByAddress(bs);
                        SocketAddress sa = new InetSocketAddress(address, 8080);  
                        
                        channel.send(bb, sa);
                        
                        
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
                
                
 		ByteBuffer byteBuffer = ByteBuffer.allocate(65535);
                
 		//while (true) {
                if (true) {
 			try {
                                int iRecvLen = 0;
                                byte[] chTemp = new byte[4096];
 				int eventsCount = selector.select(5000);
 				if (eventsCount > 0) {
 					Set selectedKeys = selector.selectedKeys();
 					Iterator iterator = selectedKeys.iterator();
 					while (iterator.hasNext()) {
 						SelectionKey sk = (SelectionKey) iterator.next();
 						iterator.remove();
 						if (sk.isReadable()) {
                                                    
 							DatagramChannel datagramChannel = (DatagramChannel) sk
 									.channel();
                                                        byteBuffer.clear();
 							SocketAddress sa = datagramChannel.receive(byteBuffer);
 							byteBuffer.flip();
 
                                                        
                                                        iRecvLen = byteBuffer.limit();
 							// 测试：通过将收到的ByteBuffer首先通过缺省的编码解码成CharBuffer 再输出马储油平台
 							CharBuffer charBuffer = Charset.forName("UTF-8").decode(byteBuffer);
                                                        Bcd2AscEx(chTemp, byteBuffer.array(), iRecvLen*2);
 							//System.out.println("receive message:"+ charBuffer.toString());
                                                        String s = new String(chTemp, "UTF-8");
                                                        System.out.println("receive message["+ iRecvLen +"]: "+ s.trim());
                                                        
 							byteBuffer.clear();
 
 							//String echo = "This is the reply message from 服务器。";
 							//ByteBuffer buffer = Charset.defaultCharset()
 							//		.encode(echo);
                                                        //chTemp = new char[2];
                                                        //chTemp[0] = 0x56;
                                                        //chTemp[1] = 0x78;
                                                        
                                                        //ByteBuffer buffer = ByteBuffer.allocate(chTemp.length);
                                                        //Charset cs = Charset.forName ("UTF-8");
                                                        //CharBuffer cb = CharBuffer.allocate (chTemp.length);
                                                        //cb.put (chTemp);
                                                        //cb.flip ();
                                                        
                                                        //ByteBuffer bb = cs.encode (cb);

                                                        
                                                        //要发送就下面打开
                                                        //datagramChannel.send(bb, sa);
 							//datagramChannel.write(buffer);
 						}
 					}
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
                
                System.out.println("TestOrm end!\n");
                 
        }
        
        
        
        // char转byte

        private byte[] getBytes (char[] chars) {
        Charset cs = Charset.forName ("UTF-8");
        CharBuffer cb = CharBuffer.allocate (chars.length);
        cb.put (chars);
                        cb.flip ();
        ByteBuffer bb = cs.encode (cb);

        return bb.array();

        }

        // byte转char

        private char[] getChars (byte[] bytes) {
            Charset cs = Charset.forName ("UTF-8");
            ByteBuffer bb = ByteBuffer.allocate (bytes.length);
            bb.put (bytes);
                        bb.flip ();
            CharBuffer cb = cs.decode (bb);

        return cb.array();
}



        public static void main(String[] args) {
 		new ReaderJavaWDemo().start();
 	}
        
 }
