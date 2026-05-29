use rapier2d::prelude::*;

pub struct PhysicsWorld {
    pub rigid_body_set: RigidBodySet,
    pub collider_set: ColliderSet,
    pub integration_parameters: IntegrationParameters,
    pub physics_pipeline: PhysicsPipeline,
    pub island_manager: IslandManager,
    pub broad_phase: BroadPhaseMultiSap,
    pub narrow_phase: NarrowPhase,
    pub contact_force_event_handler: (),
    pub contact_event_handler: (),
    pub impulse_joint_set: ImpulseJointSet,
    pub multibody_joint_set: MultibodyJointSet,
    pub ccd_solver: CCDSolver,
    pub query_pipeline: QueryPipeline,
    pub gravity: Vector<Real>,
}

impl PhysicsWorld {
    pub fn new() -> Self {
        Self {
            rigid_body_set: RigidBodySet::new(),
            collider_set: ColliderSet::new(),
            integration_parameters: IntegrationParameters::default(),
            physics_pipeline: PhysicsPipeline::new(),
            island_manager: IslandManager::new(),
            broad_phase: BroadPhaseMultiSap::new(),
            narrow_phase: NarrowPhase::new(),
            contact_force_event_handler: (),
            contact_event_handler: (),
            impulse_joint_set: ImpulseJointSet::new(),
            multibody_joint_set: MultibodyJointSet::new(),
            ccd_solver: CCDSolver::new(),
            query_pipeline: QueryPipeline::new(),
            gravity: vector![0.0, -9.81],
        }
    }

    pub fn step(&mut self) {
        self.physics_pipeline.step(
            &self.gravity,
            &self.integration_parameters,
            &mut self.island_manager,
            &mut self.broad_phase,
            &mut self.narrow_phase,
            &mut self.rigid_body_set,
            &mut self.collider_set,
            &mut self.impulse_joint_set,
            &mut self.multibody_joint_set,
            &mut self.ccd_solver,
            Some(&mut self.query_pipeline),
            &self.contact_event_handler,
            &self.contact_force_event_handler,
        );
    }
}

// --- FFI EXPORTS ---

#[no_mangle]
pub extern "C" fn st_physics_world_create() -> *mut PhysicsWorld {
    Box::into_raw(Box::new(PhysicsWorld::new()))
}

#[no_mangle]
pub extern "C" fn st_physics_world_destroy(world: *mut PhysicsWorld) {
    if !world.is_null() {
        unsafe {
            drop(Box::from_raw(world));
        }
    }
}

#[no_mangle]
pub extern "C" fn st_physics_world_step(world: *mut PhysicsWorld) {
    let world = unsafe { &mut *world };
    world.step();
}

#[no_mangle]
pub extern "C" fn st_physics_init_test() {
    println!("[RUST] Physics Engine Initialized (Rapier2D)");
}

#[no_mangle]
pub extern "C" fn st_rigid_body_create(world: *mut PhysicsWorld, body_type: i32, x: f32, y: f32, angle: f32) -> u64 {
    let world = unsafe { &mut *world };
    
    let rb_type = match body_type {
        1 => RigidBodyType::Dynamic,
        2 => RigidBodyType::KinematicVelocityBased,
        _ => RigidBodyType::Fixed,
    };

    let rigid_body = RigidBodyBuilder::new(rb_type)
        .translation(vector![x, y])
        .rotation(angle)
        .build();
    
    let handle = world.rigid_body_set.insert(rigid_body);
    let (index, generation) = handle.into_raw_parts();
    ((generation as u64) << 32) | (index as u64)
}

#[no_mangle]
pub extern "C" fn st_collider_box_create(world: *mut PhysicsWorld, body_handle: u64, half_width: f32, half_height: f32) {
    let world = unsafe { &mut *world };
    let index = (body_handle & 0xFFFFFFFF) as u32;
    let generation = (body_handle >> 32) as u32;
    let rb_handle = RigidBodyHandle::from_raw_parts(index, generation);

    let collider = ColliderBuilder::cuboid(half_width, half_height).build();
    world.collider_set.insert_with_parent(collider, rb_handle, &mut world.rigid_body_set);
}

#[no_mangle]
pub extern "C" fn st_rigid_body_get_position(world: *mut PhysicsWorld, body_handle: u64, x: *mut f32, y: *mut f32) {
    let world = unsafe { &mut *world };
    let index = (body_handle & 0xFFFFFFFF) as u32;
    let generation = (body_handle >> 32) as u32;
    let rb_handle = RigidBodyHandle::from_raw_parts(index, generation);

    if let Some(rb) = world.rigid_body_set.get(rb_handle) {
        unsafe {
            *x = rb.translation().x;
            *y = rb.translation().y;
        }
    }
}

#[no_mangle]
pub extern "C" fn st_rigid_body_get_rotation(world: *mut PhysicsWorld, body_handle: u64) -> f32 {
    let world = unsafe { &mut *world };
    let index = (body_handle & 0xFFFFFFFF) as u32;
    let generation = (body_handle >> 32) as u32;
    let rb_handle = RigidBodyHandle::from_raw_parts(index, generation);

    if let Some(rb) = world.rigid_body_set.get(rb_handle) {
        rb.rotation().angle()
    } else {
        0.0
    }
}

#[no_mangle]
pub extern "C" fn st_rigid_body_set_position(world: *mut PhysicsWorld, body_handle: u64, x: f32, y: f32) {
    let world = unsafe { &mut *world };
    let index = (body_handle & 0xFFFFFFFF) as u32;
    let generation = (body_handle >> 32) as u32;
    let rb_handle = RigidBodyHandle::from_raw_parts(index, generation);

    if let Some(rb) = world.rigid_body_set.get_mut(rb_handle) {
        rb.set_translation(vector![x, y], true);
    }
}
