package display.draw

class ModelHolder {

    private val hashMap = HashMap<ModelEnum, Model>()

    fun init() {
        ModelEnum.values()
            .forEach { hashMap[it] = Model.load(it.name) }
    }

    fun getModel(model: ModelEnum): Model = hashMap[model].let {
        checkNotNull(it) { "Cannot find model $model" }
        it.clone()
    }

}
