#pragma once
#include <string>
#include <unordered_map>
#include <typeindex>
#include <memory>

using EntityID = unsigned int;

class Component {
public:
    virtual ~Component() = default;
};

class Entity {
public:
    Entity(EntityID id, const std::string& name);
    EntityID getID() const;
    const std::string& getName() const;

    template<typename T>
    void addComponent(std::shared_ptr<T> component);

    template<typename T>
    T* getComponent();

private:
    EntityID id;
    std::string name;
    std::unordered_map<std::type_index, std::shared_ptr<Component>> components;
};
