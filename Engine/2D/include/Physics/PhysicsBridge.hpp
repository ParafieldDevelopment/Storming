#pragma once

#ifdef __cplusplus
extern "C" {
#endif

    /**
     * Opaque pointer to the Rust PhysicsWorld structure.
     */
    typedef void* st_physics_world_ptr;

    /**
     * Initializes and returns a new Rapier physics world.
     */
    st_physics_world_ptr st_physics_world_create();

    /**
     * Destroys the physics world and frees memory.
     */
    void st_physics_world_destroy(st_physics_world_ptr world);

    /**
     * Steps the physics simulation.
     */
    void st_physics_world_step(st_physics_world_ptr world);

    /**
     * Simple test function to verify the bridge.
     */
    void st_physics_init_test();

    typedef unsigned long long st_rigid_body_handle;

    st_rigid_body_handle st_rigid_body_create(st_physics_world_ptr world, int body_type, float x, float y, float angle);
    void st_collider_box_create(st_physics_world_ptr world, st_rigid_body_handle body_handle, float half_width, float half_height);
    
    void st_rigid_body_get_position(st_physics_world_ptr world, st_rigid_body_handle body_handle, float* x, float* y);
    float st_rigid_body_get_rotation(st_physics_world_ptr world, st_rigid_body_handle body_handle);
    void st_rigid_body_set_position(st_physics_world_ptr world, st_rigid_body_handle body_handle, float x, float y);

#ifdef __cplusplus
}
#endif
