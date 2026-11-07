package com.petworld;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class ApiController {
 private final List<Map<String,Object>> pets = Collections.synchronizedList(new ArrayList<>());
 private final List<Map<String,Object>> bookings = Collections.synchronizedList(new ArrayList<>());
 private final AtomicLong petIds = new AtomicLong(6), bookingIds = new AtomicLong(101);
 private static final String TOKEN = "petworld-demo-admin-token";

 public ApiController() {
  addSeed(1,"Betta Fish","Fish",250,"—","Beautiful, vibrant betta fish with flowing fins. A lovely addition to a well-maintained aquarium.","https://images.unsplash.com/photo-1524704654690-b56c05c78a00?w=900&q=85","Aquarium favourite");
  addSeed(2,"Cockatiel","Birds",3500,"Young","A cheerful and curious companion bird. Ask our team about care, diet and a suitable home.","https://images.unsplash.com/photo-1552728089-57bdde30beb3?w=900&q=85","Friendly companion");
  addSeed(3,"Syrian Hamster","Small pets",1800,"Young","An adorable little companion. We can guide you on habitat setup, bedding and nutrition.","https://images.unsplash.com/photo-1425082661705-1834bfd09dca?w=900&q=85","Tiny & adorable");
  addSeed(4,"Fancy Goldfish","Fish",180,"—","Colourful goldfish selected for home aquariums. Multiple varieties may be available in-store.","https://images.unsplash.com/photo-1522069169874-c58ec4b76be5?w=900&q=85","Popular pick");
  addSeed(5,"Lovebird","Birds",2200,"Young","Social, lively birds. Our team can help you understand companionship and daily care needs.","https://images.unsplash.com/photo-1555169062-013468b47731?w=900&q=85","Lovely personality");
  addSeed(6,"Aquarium Starter Kit","Accessories",2499,"—","A sample starter bundle for setting up your aquarium. Confirm exact contents in-store.","https://images.unsplash.com/photo-1520302519878-78e7e5c1e8b5?w=900&q=85","Shop essentials");
  Map<String,Object> b = new LinkedHashMap<>(); b.put("id",100L);b.put("name","Demo Customer");b.put("phone","9876543210");b.put("pet","Betta Fish");b.put("date","2026-11-12");b.put("time","11:00");b.put("note","Interested in availability");b.put("status","Pending");bookings.add(b);
 }
 private void addSeed(long id,String name,String category,int price,String age,String description,String image,String tag) {
  Map<String,Object> p=new LinkedHashMap<>();p.put("id",id);p.put("name",name);p.put("category",category);p.put("price",price);p.put("age",age);p.put("description",description);p.put("image",image);p.put("tag",tag);p.put("available",true);pets.add(p);
 }
 @GetMapping("/health") public Map<String,Object> health(){return Map.of("status","ok","storage","in-memory");}
 @GetMapping("/pets") public List<Map<String,Object>> getPets(){synchronized(pets){return new ArrayList<>(pets);}}
 @GetMapping("/bookings") public List<Map<String,Object>> getBookings(){synchronized(bookings){return new ArrayList<>(bookings);}}
 @PostMapping("/bookings") public ResponseEntity<?> createBooking(@RequestBody Map<String,Object> body) {
  if(blank(body.get("name"))||blank(body.get("phone"))) return ResponseEntity.badRequest().body(Map.of("message","Name and phone are required"));
  Map<String,Object> b=new LinkedHashMap<>(body);b.put("id",bookingIds.incrementAndGet());b.put("status","Pending");bookings.add(0,b);return ResponseEntity.status(201).body(b);
 }
 @PostMapping("/admin/login") public ResponseEntity<?> login(@RequestBody Map<String,String> body) {
  if("admin".equals(body.get("username"))&&"petworld123".equals(body.get("password"))) return ResponseEntity.ok(Map.of("token",TOKEN,"username","admin"));
  return ResponseEntity.status(401).body(Map.of("message","Invalid credentials"));
 }
 @PostMapping("/admin/pets") public ResponseEntity<?> createPet(@RequestHeader(value="Authorization",required=false) String auth,@RequestBody Map<String,Object> body) {
  if(!authorized(auth))return unauthorized();Map<String,Object> p=new LinkedHashMap<>(body);p.put("id",petIds.incrementAndGet());p.putIfAbsent("available",true);pets.add(p);return ResponseEntity.status(201).body(p);
 }
 @PutMapping("/admin/pets/{id}") public ResponseEntity<?> updatePet(@RequestHeader(value="Authorization",required=false) String auth,@PathVariable long id,@RequestBody Map<String,Object> body) {
  if(!authorized(auth))return unauthorized();synchronized(pets){for(int i=0;i<pets.size();i++)if(((Number)pets.get(i).get("id")).longValue()==id){Map<String,Object> p=new LinkedHashMap<>(body);p.put("id",id);pets.set(i,p);return ResponseEntity.ok(p);}}return ResponseEntity.notFound().build();
 }
 @DeleteMapping("/admin/pets/{id}") public ResponseEntity<?> deletePet(@RequestHeader(value="Authorization",required=false) String auth,@PathVariable long id) {
  if(!authorized(auth))return unauthorized();pets.removeIf(p->((Number)p.get("id")).longValue()==id);return ResponseEntity.noContent().build();
 }
 @PatchMapping("/admin/bookings/{id}/status") public ResponseEntity<?> updateBooking(@RequestHeader(value="Authorization",required=false) String auth,@PathVariable long id,@RequestBody Map<String,String> body) {
  if(!authorized(auth))return unauthorized();synchronized(bookings){for(Map<String,Object>b:bookings)if(((Number)b.get("id")).longValue()==id){b.put("status",body.getOrDefault("status","Pending"));return ResponseEntity.ok(b);}}return ResponseEntity.notFound().build();
 }
 private boolean authorized(String auth){return auth!=null&&auth.equals("Bearer "+TOKEN);}
 private ResponseEntity<?> unauthorized(){return ResponseEntity.status(401).body(Map.of("message","Admin authentication required"));}
 private boolean blank(Object o){return o==null||o.toString().isBlank();}
}
