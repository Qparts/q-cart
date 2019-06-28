package q.rest.cart.model.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

@Entity
@Table(name="crt_shipment")
public class Shipment implements Serializable{

	private static final long serialVersionUID = 1L;
	
	@Column(name="id")
	@Id
	@SequenceGenerator(name = "crt_shipment_id_seq_gen", sequenceName = "crt_shipment_id_seq", initialValue=1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "crt_shipment_id_seq_gen")
	private long id;
	@Column(name="customer_id")
	private long customerId;
	@Column(name="created")
	@Temporal(TemporalType.TIMESTAMP)
	private Date created;
	@Column(name="created_by")
	private int createdBy;
	@Column(name="courier_id")
	private int courierId;
	@Column(name="track_reference")
	private String trackReference;
	@Column(name="address_id")
	private long addressId;
	@Column(name="trackable")
	private boolean trackable;
	@Column(name="status")
	private char status;
	@Column(name="shipment_fees")
	private double shipmentFees;
	@Column(name="bound")
	private Character bound;
	@Transient
	private List<ShipmentItem> shipmentItems;
	
	public List<ShipmentItem> getShipmentItems() {
		return shipmentItems;
	}
	public void setShipmentItems(List<ShipmentItem> shipmentItems) {
		this.shipmentItems = shipmentItems;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getCustomerId() {
		return customerId;
	}
	public void setCustomerId(long customerId) {
		this.customerId = customerId;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public int getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(int createdBy) {
		this.createdBy = createdBy;
	}
	public int getCourierId() {
		return courierId;
	}
	public void setCourierId(int courrierId) {
		this.courierId = courrierId;
	}
	public String getTrackReference() {
		return trackReference;
	}
	public void setTrackReference(String trackReference) {
		this.trackReference = trackReference;
	}
	public long getAddressId() {
		return addressId;
	}
	public void setAddressId(long addressId) {
		this.addressId = addressId;
	}
	public boolean isTrackable() {
		return trackable;
	}
	public void setTrackable(boolean trackable) {
		this.trackable = trackable;
	}
	public char getStatus() {
		return status;
	}
	public void setStatus(char status) {
		this.status = status;
	}
	public double getShipmentFees() {
		return shipmentFees;
	}
	public void setShipmentFees(double shipmentFees) {
		this.shipmentFees = shipmentFees;
	}
	public Character getBound() {
		return bound;
	}
	public void setBound(Character bound) {
		this.bound = bound;
	}
	
	
	
}
