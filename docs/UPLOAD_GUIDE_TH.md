# วิธีอัปโหลดขึ้น GitHub (Private ก่อน)

1. สร้าง Repository ใหม่ เช่น `java-room-equipment-booking-portfolio` โดยตั้ง **Private** และไม่ต้อง Add README.
2. แตก ZIP ที่ได้รับ แล้วเปิดโฟลเดอร์ที่แตกออกมาใน Dolphin.
3. กด `Ctrl + H` เพื่อแสดง `.gitignore` จากนั้นเลือก **เนื้อหาภายใน** โฟลเดอร์ทั้งหมด (`Ctrl + A`).
4. ในหน้า Repository ว่าง ให้กด **uploading an existing file** แล้วลากไฟล์และโฟลเดอร์ไปวาง (ไม่ใช่ลาก ZIP ทั้งก้อน).
5. ตรวจว่ามี `README.md`, `pom.xml`, `.gitignore`, `src/main/`, `images/`, `docs/` และ **ไม่มี** `meeting_room_booking.db` หรือไฟล์ `.class`.
6. กด **Commit changes** แล้วกลับหน้า Code ตรวจจำนวนไฟล์และ README.
7. ยังไม่ต้องเปิด Public จนตรวจสิทธิ์โครงงานทีมและทรัพย์สินทางปัญญาของภาพ/เอกสารครบ.

**หมายเหตุสำคัญ:** หาก Repository เก่าเคยอัปโหลดไฟล์ `.db` ไปแล้ว แม้ลบออกจากหน้าเว็บในปัจจุบัน ข้อมูลก็ยังอาจอยู่ใน Commit เก่า แนะนำสร้าง Repo ใหม่สำหรับ Portfolio.
