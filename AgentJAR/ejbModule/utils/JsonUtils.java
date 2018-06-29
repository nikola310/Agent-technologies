package utils;

import java.io.Serializable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.json.JSONObject;

import messages.MDBConsumer;
import model.acl.ACLMessage;
import model.agent.AID;
import model.agent.AgentType;
import model.center.AgentCenter;

/**
 * @author Nikola
 *
 */
public class JsonUtils {

	public static String getACLMessageString(ACLMessage msg) {
		JSONObject obj = new JSONObject();
		obj.append("type", "acl_message");
		obj.append("data", new JSONObject(msg));
		return obj.toString();
	}

	public static String getAIDString(AID aid, boolean start) {
		JSONObject obj = new JSONObject();
		if (start)
			obj.append("type", "start_agent");
		else
			obj.append("type", "stop_agent");
		obj.append("data", new JSONObject(aid));
		return obj.toString();
	}

	public static String getAgentType(AgentType at, boolean start) {
		JSONObject obj = new JSONObject();
		if (start)
			obj.append("type", "add_agent_type");
		else
			obj.append("type", "remove_agent_type");
		obj.append("data", new JSONObject(at));
		return obj.toString();
	}

	public static String getNodeRequestType(String nodeRequest) {
		JSONObject obj = new JSONObject(nodeRequest);
		return obj.getString("type");
	}

	public static AgentCenter getNodeRequestSlaveAddres(String nodeRequest) {
		JSONObject obj = new JSONObject(nodeRequest);
		JSONObject jdata = obj.getJSONObject("data");
		AgentCenter ac = new AgentCenter();
		ac.setAddress(jdata.getString("address"));
		ac.setAlias(jdata.getString("alias"));
		return ac;
	}

	public static boolean sendACL(ACLMessage msg) {
		try {

			Context context = new InitialContext();
			ConnectionFactory cf = (ConnectionFactory) context.lookup(MDBConsumer.REMOTE_FACTORY);
			final Queue queue = (Queue) context.lookup(MDBConsumer.MDB_CONSUMER_QUEUE);
			context.close();
			Connection connection = cf.createConnection();
			final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			connection.start();

			ObjectMessage tmsg = session.createObjectMessage((Serializable) msg);
			MessageProducer producer = session.createProducer(queue);
			producer.send(tmsg);
			producer.close();
			connection.stop();
			connection.close();
			return true;
		} catch (NamingException | JMSException e) {
			e.printStackTrace();
			return false;
		}
	}

}
