package com.hdconsulting.springboot.reactor.app;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.hdconsulting.springboot.reactor.app.models.Comentarios;
import com.hdconsulting.springboot.reactor.app.models.Usuario;
import com.hdconsulting.springboot.reactor.app.models.UsuarioComentarios;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// exempleCollectList();
		// exempleToString();
		ejemploContraPresion();
	}
	
	public void ejemploContraPresion() {
		//pour limiter les données reçus soit
		// on on defini Subscriber
		//soit on appel la méthode limiteRate
		
		Flux.range(1, 10)
		.log()
		.limitRate(5)
		.subscribe(/*new Subscriber<Integer>() {

			private Subscription s;
			private Integer limite = 2;
			private Integer consumido = 0;
			@Override
			public void onSubscribe(Subscription s) {
				this.s = s;
				//Pour envoyer le maximum possible
				//s.request(Long.MAX_VALUE);
				s.request(limite);
				
			}

			@Override
			public void onNext(Integer t) {
				log.info(t.toString());
				consumido++;
				
				if (consumido == limite) {
					consumido = 0;
					s.request(limite);
				}
				
			}

			@Override
			public void onError(Throwable t) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onComplete() {
				// TODO Auto-generated method stub
				
			}
		}*/);
		
	}
	
	public void ejemploIntervalDesdeCreate() {
		Flux.create(emitter -> {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				private Integer contador = 0;

				@Override
				public void run() {
					emitter.next(++contador);
					if (contador == 10) {
						timer.cancel();
						emitter.complete();
					}
					
				}
				
			}, 1000, 1000);
		})
		.doOnNext(next -> log.info(next.toString()))
		.doOnComplete(() -> log.info("Hemos terminado"))
		.subscribe();
		
	}
	
	public void ejemploIntervaloInfinito() throws InterruptedException {
		
		CountDownLatch latch = new CountDownLatch(1);
		
		Flux.interval(Duration.ofSeconds(1))
		//.doOnTerminate(() -> latch.countDown())
		.doOnTerminate(latch::countDown)
		.flatMap(i -> {
			if (i > 5)
				return Flux.error(new InterruptedException("Solo hasta 5"));
			else return Flux.just(i);
		})
		.map(i -> "Hola " + i)
		.retry(2) //re-essayer deux fois s'il y a des erreurs
		.subscribe(s -> log.info(s), e -> log.error(e.getMessage()));
		
		latch.await();
	}
	
	public void exemploDelayElements() {
		Flux<Integer> rango = Flux.range(1, 12)
				.delayElements(Duration.ofSeconds(1))
				.doOnNext((i -> log.info(i.toString())));
		
		rango.blockLast();
		
		
	}
	
	public void exemploInterval() {
		Flux<Integer> rango = Flux.range(1, 12);
		Flux<Long> retraso = Flux.interval(Duration.ofSeconds(1));
		
		rango.zipWith(retraso, (ra, re) -> ra)
		.doOnNext(i -> log.info(i.toString()))
		.blockLast();
		
	}
	
	public void exemploZipWithRango() {
		
		Flux.just(1, 2, 3, 4)
		.map(i -> (i * 2) )
		.zipWith(Flux.range(0, 4), (uno, dos) -> String.format("Primero Flux: %d, Segundo Flux: %d", uno, dos))
		.subscribe(texto -> log.info(texto));
	}
	
	public void exemploUsuarioZipWithForma2() {

		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("John", "Snow"));

		Mono<Comentarios> comentariosMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();

			comentarios.addComentario("Hola Pepe, que tal");
			comentarios.addComentario("Mañana voy a la playa");
			comentarios.addComentario("Estoy creando mi empresa, para poder ser milionario");

			return comentarios;
		});
		
	Mono<UsuarioComentarios> usuariosComentarios =	usuarioMono
			.zipWith(comentariosMono)
			.map(tuple ->{
				Usuario u = tuple.getT1();
				Comentarios c = tuple.getT2();
				return new UsuarioComentarios(u, c);
			});
	
	usuariosComentarios.subscribe(uc -> log.info(uc.toString()));

	}

	public void exemploUsuarioZipWith() {

		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("John", "Snow"));

		Mono<Comentarios> comentariosMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();

			comentarios.addComentario("Hola Pepe, que tal");
			comentarios.addComentario("Mañana voy a la playa");
			comentarios.addComentario("Estoy creando mi empresa, para poder ser milionario");

			return comentarios;
		});
		
	Mono<UsuarioComentarios> usuariosComentarios =	usuarioMono
			.zipWith(comentariosMono, (usuarios, comentario) -> new UsuarioComentarios(usuarios, comentario));
	usuariosComentarios.subscribe(uc -> log.info(uc.toString()));
//		usuarioMono.flatMap(u -> comentariosMono.map(c -> new UsuarioComentarios(u, c)))
//				.subscribe(uc -> log.info(uc.toString()));

	}

	public void exemploUsuarioFlatMap() {

		Mono<Usuario> usuarioMono = Mono.fromCallable(() -> new Usuario("John", "Snow"));

		Mono<Comentarios> comentariosMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();

			comentarios.addComentario("Hola Pepe, que tal");
			comentarios.addComentario("Mañana voy a la playa");
			comentarios.addComentario("Estoy creando mi empresa, para poder ser milionario");

			return comentarios;
		});

		usuarioMono.flatMap(u -> comentariosMono.map(c -> new UsuarioComentarios(u, c)))
				.subscribe(uc -> log.info(uc.toString()));

	}

	public void exempleCollectList() throws Exception {
		List<Usuario> usuariosList = new ArrayList<>();

		usuariosList.add(new Usuario("Hugo Fulano"));
		usuariosList.add(new Usuario("Willyam Fulana"));
		usuariosList.add(new Usuario("Kew sultano"));
		usuariosList.add(new Usuario("Teety sultana"));
		usuariosList.add(new Usuario("Bruce Lee"));
		usuariosList.add(new Usuario("Bruce Willis"));

		Flux.fromIterable(usuariosList).collectList()
				.subscribe(lista -> lista.forEach(item -> log.info(item.toString())));
	}

	public void exempleToString() throws Exception {

		List<Usuario> usuariosList = new ArrayList<>();

		usuariosList.add(new Usuario("Hugo Fulano"));
		usuariosList.add(new Usuario("Willyam Fulana"));
		usuariosList.add(new Usuario("Kew sultano"));
		usuariosList.add(new Usuario("Teety sultana"));
		usuariosList.add(new Usuario("Bruce Lee"));
		usuariosList.add(new Usuario("Bruce Willis"));

		Flux.fromIterable(usuariosList)
				.map(usuario -> usuario.getNombre().toUpperCase().concat(" ")
						.concat(usuario.getApellido().toUpperCase()))
				// .filter(usuario -> usuario.getNombre().equalsIgnoreCase("bruce"))
				.flatMap(nombre -> {
					if (nombre.contains("bruce".toUpperCase()))
						return Mono.just(nombre);
					return Mono.empty();
				}).map(nombre -> {

					return nombre.toLowerCase();
				}).subscribe(nombre -> log.info(nombre));

	}

	public void exempleFlatMat() throws Exception {
		List<String> usuariosList = new ArrayList<>();

		usuariosList.add("Hugo Fulano");
		usuariosList.add("Willyam Fulana");
		usuariosList.add("Kew sultano");
		usuariosList.add("Teety sultana");
		usuariosList.add("Bruce Lee");
		usuariosList.add("Bruce Willis");

		Flux.fromIterable(usuariosList).map(nombre -> new Usuario(nombre))
				// .filter(usuario -> usuario.getNombre().equalsIgnoreCase("bruce"))
				.flatMap(usuario -> {
					if (usuario.getNombre().equalsIgnoreCase("bruce"))
						return Mono.just(usuario);
					return Mono.empty();
				}).map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				}).map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				}).subscribe(usuario -> log.info(usuario.toString()));
	}

	public void exempleIterable() throws Exception {
		List<String> usuariosList = new ArrayList<>();

		usuariosList.add("Hugo Fulano");
		usuariosList.add("Willyam Fulana");
		usuariosList.add("Kew sultano");
		usuariosList.add("Teety sultana");
		usuariosList.add("Bruce Lee");
		usuariosList.add("Bruce Wilis");

		Flux<String> nombres = Flux.fromIterable(usuariosList);// Flux.just("Hugo Fulano", "Willyam Fulana", "Kew
																// sultano", "Teety sultana", "Bruce Lee", "Bruce
																// Wilis")
		Flux<Usuario> Usuarios = nombres.map(nombre -> new Usuario(nombre))
				.filter(usuario -> usuario.getNombre().equalsIgnoreCase("bruce")).doOnNext(usuario -> {
					if (usuario == null) {
						throw new RuntimeException("El nombre no puede ser vacio");
					}
					System.out.println(usuario.getNombre());

				}).map(usuario -> {
					String nombre = usuario.getNombre().toLowerCase();
					usuario.setNombre(nombre);
					return usuario;
				});

		Usuarios.subscribe(usuario -> log.info(usuario.toString()), error -> log.error(error.getMessage()),
				new Runnable() {

					@Override
					public void run() {
						log.info("Ha finalisado la ejecution del observable con exito");

					}
				});
	}
}
