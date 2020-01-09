extends Node2D

# Declare member variables here. Examples:
# var a = 2
# var b = "text"

# Called when the node enters the scene tree for the first time.
func _ready():
	$Player/HUD.show()
	$Player/HUD.show_message("Go!")

func _process(delta):
	pass


func _on_DeathZone_body_entered(body):
	if body == $Player:
		$Player/HUD.show_message("You Died!")
		var t = Timer.new()
		t.set_wait_time(1.25)
		t.set_one_shot(true)
		self.add_child(t)
		t.start()
		yield(t, "timeout")
		get_tree().change_scene("res://scenes/menumap/MainMenu.tscn")
		t.queue_free()
