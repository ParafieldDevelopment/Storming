#pragma once
#include "Entity.h"
#include <vector>
#include <unordered_map>

class ECSManager {
public:
    EntityID createEntity(const std::string& name);
    void removeEntity(EntityID id);
    Entity* getEntity(EntityID id);

private:
    std::unordered_map<EntityID, Entity> entities;
    EntityID nextID = 1;
};
