# Architecture : De Windows (Python) à Android (Kotlin)

Ce document décrit les choix architecturaux effectués lors du portage de l'application BOP de Windows vers Android.

## Motivations
Le passage à Android vise à offrir une portabilité maximale aux musiciens, permettant de travailler n'importe où avec une interface tactile optimisée et une intégration native des services système.

## Comparaison des Technologies

| Composant | Version Windows (Originale) | Version Android (Actuelle) |
| :--- | :--- | :--- |
| **Langage** | Python 3 | Kotlin 1.9+ |
| **Interface** | Tkinter / Custom UI | Jetpack Compose (Déclaratif) |
| **Moteur Audio** | VLC / Pygame | Jetpack Media3 (ExoPlayer) |
| **Persistence** | Fichiers JSON locaux | SharedPreferences / JSON Serialized |
| **Accessibilité** | Lecteurs d'écran système | TalkBack + Étiquettes Sémantiques |

## Structure du Projet (Modèle MVVM)
L'application suit les principes de l'architecture propre Android :
- **UI Layer** : Composables Jetpack Compose réagissant au `StateFlow` du ViewModel.
- **ViewModel** : Gère la logique métier, l'état de lecture et les coroutines pour les sessions de pratique.
- **Infrastructure Layer** : Abstraction de Media3 (`AudioPlayerManager`) et gestion des fichiers de segments.
- **Core Layer** : Entités pures (Segment, PracticeSession) indépendantes du framework Android.

## Gestion Audio
L'utilisation de **Media3** permet de gérer nativement le pitch et le tempo via les `PlaybackParameters`, garantissant une qualité audio supérieure sans latence excessive sur mobile.
