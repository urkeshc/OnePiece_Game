### **One Piece: Thousand Sunny - A Thrilling Adventure in the Grand Line!** 🌊🏴‍☠️

Welcome to the game. This is a game I created, written in Java, using the MVC architecture. As long as you have a JVM on your machine, as well as `lombok` installed, you should be able to run it. Make sure to read the instructions to run it properly.

---

## **Objective**
- Survive and advance through levels set in iconic One Piece locations like **Sabaody Archipelago** and **Skypiea**.
- Defeat **Marine Ships**, **Kizaru**, and **Enel** using powerful weapons like Cannonballs and the devastating **Luffy Ball (Nuke)**.
- Collect **Devil Fruits** to strengthen your ship and **Straw Hats** to recharge your nukes.
- Protect the **Thousand Sunny** by managing its health, shield, and ammunition.

---

## **Controls**
- **Arrow Keys**:
  - **Up**: Accelerate (Thrust)
  - **Left/Right**: Turn
- **Space Bar**: Fire cannonballs
- **F**: Launch a Luffy Ball (Nuke)
- **P**: Pause the game
- **Q**: Quit the game
- **M**: Toggle background music
- **A**: Toggle radar

---

## **Gameplay Mechanics**

### **Levels**
- Progress through 3 levels:
  1. **Ocean Battle** - Fight off Marine Ships and dodge asteroids.
  2. **Sabaody Archipelago** - Face Kizaru amidst chaotic battles.
  3. **Skypiea** - Confront Enel, the final boss.

### **Enemies**
- **Marine Ships**: Fire cannonballs at you. Dodge and retaliate!
- **Rocks**: Avoid these dangerous obstacles.
- **Kizaru Boss**: A light-speed menace with devastating attacks.
- **Enel Boss**: Beware of his thunderous might!

### **Power-Ups**
- **Devil Fruits**: Grant temporary boosts to your shields or abilities.
- **Straw Hats**: Recharge your Luffy Ball nukes (maximum of 3).

### **Strategy**
- **Health Management**: The Thousand Sunny starts with 100 health points. Keep an eye on the health bar in the center of the screen.
- **Shield**: Collect Devil Fruits to temporarily protect the ship.
- **Radar**: Toggle the radar to track enemies and objectives on a mini-map.
- **Cinematics**: Enjoy immersive cutscenes when advancing to new levels or defeating bosses.

---

## **Customization**
- **Difficulty Levels**: Select **Easy**, **Medium**, or **Hard** at the start of the game to match your skill level. Changing the level dynamically adjusts the health level of bosses as well as the number of foes.
- **Background Music**: Toggle music on or off using the `M` key.

---

## **Running the Game**
1. Install the JDK and ensure `lombok.jar` is accessible.
2. Ensure you have the project structure, including the `sources.txt` file and the `lombok.jar`. Make sure you are in the root directory of the project (`OnePiece_Game`).
3. Use these commands to compile and run the game:

```bash
rm -rf out
mkdir out
javac -cp ".:lib/lombok.jar" -d out @sources.txt
java -cp ".:out:lib/lombok.jar:resources" mvc.controller.Game
