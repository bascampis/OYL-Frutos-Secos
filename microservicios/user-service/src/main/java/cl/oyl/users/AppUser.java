package cl.oyl.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity @Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class AppUser {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String name;
    @Column(nullable = false) private String email;
    private String phone;
    private String address;
    private String role;
    @JsonIgnore private String salt;
    @JsonIgnore private String passwordHash;

    public AppUser() {}
    public Long getId() { return id; }
    public String getName() { return name; }
    public void setName(String v) { name = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { email = v; }
    public String getPhone() { return phone; }
    public void setPhone(String v) { phone = v; }
    public String getAddress() { return address; }
    public void setAddress(String v) { address = v; }
    public String getRole() { return role; }
    public void setRole(String v) { role = v; }
    public String getSalt() { return salt; }
    public void setSalt(String v) { salt = v; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String v) { passwordHash = v; }
}
