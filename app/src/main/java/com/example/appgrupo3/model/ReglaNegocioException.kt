package com.example.appgrupo3.model

open class ReglaNegocioException(mensaje: String) : Exception(mensaje)

class TagDuplicadoException(val existente: Equipo) :
    ReglaNegocioException("El TAG ${existente.tag} ya existe en esta instalación (RN-01)")
