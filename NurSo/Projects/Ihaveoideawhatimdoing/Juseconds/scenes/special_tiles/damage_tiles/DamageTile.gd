extends Node2D

# Called when the node enters the scene tree for the first time.
func _ready():
	pass # Replace with function body.

# Called every frame. 'delta' is the elapsed time since the previous frame.
func _process(delta):
	for body in $Area2D.get_overlapping_bodies():
		if body.is_in_group("player"):
			body.take_damage(15 * delta)

