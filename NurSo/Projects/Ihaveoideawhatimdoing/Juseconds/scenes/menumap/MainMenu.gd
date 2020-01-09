extends Control

# Declare member variables here. Examples:
# var a = 2
# var b = "text"

# Called when the node enters the scene tree for the first time.
func _ready():
	$AnimatedSprite/AnimationPlayer.play("MainMenu Runner")
	pass # Replace with function body.


func _process(delta):
	$ParallaxBackground/ParallaxLayer.motion_offset.x -= 1


func _on_StartButton_pressed():
	get_tree().change_scene("res://scenes/levels/TestMap.tscn")
	


func _on_QuitButton_pressed():
	get_tree().quit()

